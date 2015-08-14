package net.chaimov.orc.agent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

/**
 * Created by nchaimov on 8/7/15.
 */
public class FileStreamInstrumentationTransformer implements ClassFileTransformer {

    private final ClassPool pool;

    public FileStreamInstrumentationTransformer() {
        this.pool = ClassPool.getDefault();
        this.pool.importPackage("net.chaimov.orc.agent");
    }

    private void instrumentInputOpen(CtMethod m) throws CannotCompileException {
        m.addLocalVariable("$open_started_time$", CtClass.longType);
        m.addLocalVariable("$open_finished_time$", CtClass.longType);
        m.insertBefore("$open_started_time$ = System.nanoTime(); ");
        m.insertAfter("$open_finished_time$ = System.nanoTime();" +
                "net.chaimov.orc.agent.FileStreamStatistics#openedInputFile(" +
                "$1, $open_finished_time$ - $open_started_time$);");
    }

    private void instrumentInputClose(CtMethod m) throws CannotCompileException {
        m.insertBefore("net.chaimov.orc.agent.FileStreamStatistics#closedInputFile(this.path, this.closed);");
    }

    private void instrumentRead(CtMethod m) throws CannotCompileException {
        m.addLocalVariable("$read_started_time$", CtClass.longType);
        m.addLocalVariable("$read_finished_time$", CtClass.longType);
        m.insertBefore("$read_started_time$ = System.nanoTime(); ");
        m.insertAfter("$read_finished_time$ = System.nanoTime();" +
                "net.chaimov.orc.agent.FileStreamStatistics#readInputFile(" +
                "this.path, $read_finished_time$ - $read_started_time$);");
    }

    private void instrumentWrite(CtMethod m) throws CannotCompileException {
        if(Modifier.isNative(m.getModifiers())) {
            return;
        }
        m.addLocalVariable("$write_started_time$", CtClass.longType);
        m.addLocalVariable("$write_finished_time$", CtClass.longType);
        m.insertBefore("$write_started_time$ = System.nanoTime(); ");
        m.insertAfter("$write_finished_time$ = System.nanoTime();" +
                "net.chaimov.orc.agent.FileStreamStatistics#writeOutputFile(" +
                "this.path, $write_finished_time$ - $write_started_time$);");
    }

    private void instrumentOutputOpen(CtMethod m) throws CannotCompileException {
        m.addLocalVariable("$open_started_time$", CtClass.longType);
        m.addLocalVariable("$open_finished_time$", CtClass.longType);
        m.insertBefore("$open_started_time$ = System.nanoTime(); ");
        m.insertAfter("$open_finished_time$ = System.nanoTime();" +
                "net.chaimov.orc.agent.FileStreamStatistics#openedOutputFile(" +
                "$1, $open_finished_time$ - $open_started_time$);");
    }

    private void instrumentOutputClose(CtMethod m) throws CannotCompileException {
        m.insertBefore("net.chaimov.orc.agent.FileStreamStatistics#closedOutputFile(this.path, this.closed);");
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        if (className.endsWith("FileInputStream")) {
            try {
                CtClass cc = pool.get(className.replaceAll("/", "."));

                // open
                CtMethod[] ms = cc.getDeclaredMethods("open");
                for(CtMethod m : ms) {
                    instrumentInputOpen(m);
                }

                // close
                ms = cc.getDeclaredMethods("close");
                for(CtMethod m : ms) {
                    instrumentInputClose(m);
                }

                // read
                ms = cc.getDeclaredMethods("read");
                for(CtMethod m : ms) {
                    instrumentRead(m);
                }

                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (className.endsWith("FileOutputStream")) {
            try {
                CtClass cc = pool.get(className.replaceAll("/", "."));

                //open
                CtMethod[] ms = cc.getDeclaredMethods("open");
                for(CtMethod m : ms) {
                    instrumentOutputOpen(m);
                }

                // close
                ms = cc.getDeclaredMethods("close");
                for(CtMethod m : ms) {
                    instrumentOutputClose(m);
                }

                // write
                ms = cc.getDeclaredMethods("write");
                for(CtMethod m : ms) {
                    instrumentWrite(m);
                }

                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return byteCode;

    }
}
