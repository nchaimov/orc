package net.chaimov.orc.agent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by nchaimov on 8/7/15.
 */

public class OrcAgent {

    @SuppressWarnings("unused")
    public static void premain(String args, Instrumentation inst) {
        inst.addTransformer(new FileStreamInstrumentationTransformer(), true);
        System.out.println("OrcAgent is registered.");

        if(!inst.isRetransformClassesSupported()) {
            System.err.println("WARNING: JVM does not support class retransformation.");
        } else {
            try {
                inst.retransformClasses(FileInputStream.class, FileOutputStream.class);
            } catch (UnmodifiableClassException e) {
                System.err.println("Unable to modify FileStream classes.");
                e.printStackTrace();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.err.println();
                System.err.println(FileStreamStatistics.asString());
            }
        }));

    }

    @SuppressWarnings("unused")
    public static void agentmain(String args, Instrumentation inst) {
        premain(args, inst);
    }
}
