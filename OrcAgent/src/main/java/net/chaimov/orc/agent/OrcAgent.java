package net.chaimov.orc.agent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by nchaimov on 8/7/15.
 */

public class OrcAgent {

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

    /**
     * Forces the initialization of the class pertaining to
     * the specified <tt>Class</tt> object.
     * This method does nothing if the class is already
     * initialized prior to invocation.
     *
     * @param klass the class for which to force initialization
     * @return <tt>klass</tt>

     */
    public static <T> Class<T> forceInit(Class<T> klass) {
        try {
            Class.forName(klass.getName(), true, klass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);  // Can't happen
        }
        return klass;
    }


}
