package net.chaimov.orc.agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
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


    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        if (className.endsWith("FileInputStream")) {
            try {
                CtClass cc = pool.get(className.replaceAll("/", "."));
                CtMethod m = cc.getDeclaredMethod("open");
                m.addLocalVariable("$open_started_time$", CtClass.longType);
                m.addLocalVariable("$open_finished_time$", CtClass.longType);
                m.insertBefore("$open_started_time$ = System.nanoTime(); ");
                m.insertAfter("$open_finished_time$ = System.nanoTime(); net.chaimov.orc.agent.FileStreamStatistics#openedInputFile($1, $open_finished_time$ - $open_started_time$);");
                m = cc.getDeclaredMethod("close");
                m.insertBefore("net.chaimov.orc.agent.FileStreamStatistics#closedInputFile(this.path, this.closed);");
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (className.endsWith("FileOutputStream")) {
            try {
                CtClass cc = pool.get(className.replaceAll("/", "."));
                CtMethod m = cc.getDeclaredMethod("open");
                m.addLocalVariable("$open_started_time$", CtClass.longType);
                m.addLocalVariable("$open_finished_time$", CtClass.longType);
                m.insertBefore("$open_started_time$ = System.nanoTime(); ");
                m.insertAfter("$open_finished_time$ = System.nanoTime(); net.chaimov.orc.agent.FileStreamStatistics#openedOutputFile($1, $open_finished_time$ - $open_started_time$);");
                m = cc.getDeclaredMethod("close");
                m.insertBefore("net.chaimov.orc.agent.FileStreamStatistics#closedOutputFile(this.path, this.closed);");
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return byteCode;

    }
}
