package net.chaimov.orc.agent;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by nchaimov on 8/7/15.
 */
public class OrcClient {

   public static Options options = new Options();

   public static void main(String[] args) throws IOException {
       System.out.println("This is OrcClient.");

       JCommander jc = new JCommander(options, args);
       jc.setProgramName("OrcClient");

       if(options.help) {
           jc.usage();
           System.exit(0);
       }

       if(options.wrapped) {
           runWithInstrumentedStreams();
           System.err.println();
           System.err.println(FileStreamStatistics.asString());
       } else {
           runWithBuiltinStreams();
       }

   }

    private static void runWithBuiltinStreams() {
        final List<Thread> threads = new LinkedList<Thread>();
        final LinkedList<String> files = new LinkedList<String>();

        for(int i = 0; i < options.iterations; ++i) {
            final int finalI = i;
            final String outname = "bar_" + finalI;
            files.add(outname);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        FileInputStream fis = new FileInputStream("foo");
                        long len = fis.getChannel().size();
                        byte[] contents = new byte[(int) len];
                        fis.read(contents);
                        fis.close();
                        FileOutputStream fos = new FileOutputStream(outname);
                        fos.write(contents);
                        fos.close();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threads.add(t);
        }

        for(Thread t : threads) {
            t.start();
        }

        for(Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!options.noDelete) {
            for (String s : files) {
                File f = new File(s);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    private static void runWithInstrumentedStreams() {
        final List<Thread> threads = new LinkedList<Thread>();
        final LinkedList<String> files = new LinkedList<String>();

        for (int i = 0; i < options.iterations; ++i) {
            final int finalI = i;
            final String outname = "bar_" + finalI;
            files.add(outname);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        InstrumentedFileInputStream fis = new InstrumentedFileInputStream("foo");
                        long len = fis.getChannel().size();
                        byte[] contents = new byte[(int) len];
                        fis.read(contents);
                        fis.close();
                        InstrumentedFileOutputStream fos = new InstrumentedFileOutputStream(outname);
                        fos.write(contents);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threads.add(t);
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!options.noDelete) {
            for (String s : files) {
                File f = new File(s);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    public static class Options {
        @Parameter(names = {"-i", "--iterations"}, description = "Number of times to run the benchmark.")
        public Integer iterations = 100;

        @Parameter(names = {"-w", "--wrapped"}, description = "Use wrapped streams instead of built-in.")
        public boolean wrapped = false;

        @Parameter(names = {"-h", "-?", "--help"}, description = "Print usage information.")
        public boolean help = false;

        @Parameter(names = {"-d", "--no-delete"}, description = "Leave written files after run")
        public boolean noDelete = false;

        @Parameter()
        public List<String> args;
    }

}
