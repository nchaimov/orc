package net.chaimov.orc;

/**
 * Created by nchaimov on 8/4/15.
 */

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Orc {

    public static class Options {
        @Parameter(names = {"-v", "--verbose"}, description = "Print extra details as benchmark runs.")
        public boolean verbose = false;

        @Parameter(names = {"-h", "-?", "--help"}, description = "Print usage information.", help = true)
        public boolean help = false;

        @Parameter(names = {"-i", "--iterations"}, description = "Number of times to run the benchmark.")
        public Integer iterations = 1;

        @Parameter(names = {"-r", "--reads"}, description = "Number of times to read each file.")
        public Integer reads = 10000;

        @Parameter(names = {"-e", "--seed"}, description = "Seed for random number generator.")
        public Long seed = null;

        @Parameter(names = {"-s", "--size"}, description = "Size of each read.")
        public Long size = 4096L;

        @Parameter(names = {"-f", "--file"}, description = "Base name of output file.")
        public String filename = "orcfile";

        @Parameter()
        public List<String> args;
    }

    public static Options options = new Options();

    public static void log(String msg) {
        if(options.verbose) {
            System.out.println(msg);
        }
    }

    public static void main(String[] args) {
        JCommander jc = new JCommander(options, args);
        jc.setProgramName("Orc");

        if(options.help) {
            jc.usage();
            System.exit(0);
        }

        if(options.verbose) {
            System.out.println("Verbose logging enabled.");
        }

        DescriptiveStatistics orcStats  = new DescriptiveStatistics();
        DescriptiveStatistics ormcStats = new DescriptiveStatistics();

        for(int i = 0; i < options.iterations; ++i) {
            log(MessageFormat.format("Iteration {0}", i));
            String filename = options.filename + MessageFormat.format("_{0}", i);
            boolean createSuccess = createFile(filename, options.size * options.reads);
            long orcCumulative = 0L;
            long ormcCumulative = 0L;
            if(createSuccess) {
                if(i % 2 == 0) {
                    // ORC
                    long before = System.nanoTime();
                    boolean orcSuccess = openReadClose(filename);
                    long after = System.nanoTime();
                    orcCumulative += (after - before);

                    // ORmC
                    before = System.nanoTime();
                    boolean ormcSuccess = openReadManyClose(filename);
                    after = System.nanoTime();
                    ormcCumulative += (after - before);
                } else {
                    // ORmC
                    long before = System.nanoTime();
                    boolean ormcSuccess = openReadManyClose(filename);
                    long after = System.nanoTime();
                    ormcCumulative += (after - before);

                    // ORC
                    before = System.nanoTime();
                    boolean orcSuccess = openReadClose(filename);
                    after = System.nanoTime();
                    orcCumulative += (after - before);

                }

                boolean deleteSuccess = deleteFile(filename);
            }

            log(MessageFormat.format("Time for n(ORC):\t{1,number,#}", options.reads, orcCumulative));
            log(MessageFormat.format("Time for O(nR)C:\t{1,number,#}", options.reads, ormcCumulative));

            orcStats.addValue(orcCumulative / (double) options.reads);
            ormcStats.addValue(ormcCumulative / (double) options.reads);
        }

        System.out.println();
        System.out.println("n(ORC): ");
        System.out.println(orcStats.toString());
        System.out.println("O(nR)C: ");
        System.out.println(ormcStats.toString());
        System.out.println();

        double slowdown = orcStats.getMean() / ormcStats.getMean();
        System.out.println(MessageFormat.format("ORC slowdown is {0,number,#}", slowdown));

    }

    public static boolean createFile(String path, long size) {
        log(MessageFormat.format("Creating file {0} of size {1}", path, size));
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        boolean result = true;
        try {
            Random r = new Random();
            if(options.seed != null) {
                r.setSeed(options.seed);
            }
            fos = new FileOutputStream(path);
            bos = new BufferedOutputStream(fos);
            for(long i = 0; i < size; ++i) {
                bos.write(r.nextInt());
            }
        } catch (FileNotFoundException e) {
            System.err.println(MessageFormat.format("Unable to create test file {0}", path));
            e.printStackTrace();
            result = false;
        } catch (IOException e) {
            System.err.println(MessageFormat.format("Unable to write to test file {0}", path));
            e.printStackTrace();
            result = false;
        } finally {
            if(bos != null) {
                try {
                    bos.flush();
                    fos.getFD().sync();
                    bos.close();
                } catch (IOException e) {
                    System.err.println(MessageFormat.format("Unable to close test file {0}", path));
                    e.printStackTrace();
                    result = false;
                }
            }
        }
        return result;
    }

    public static boolean deleteFile(String path) {
        log(MessageFormat.format("Deleting file {0}", path));
        File f = new File(path);
        boolean result = true;
        try {
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            System.err.println(MessageFormat.format("Unable to delete file {0}", path));
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public static byte[] bytes;

    public static boolean openReadClose(String path) {
        log("Doing n(ORC) cycles.");
        boolean result = true;
        try {
            for (int i = 0; i < options.reads; ++i) {
                File f = new File(path);
                long fsize = options.size;
                FileInputStream fis = new FileInputStream(f);
                fis.getChannel().position(i*fsize);
                bytes = new byte[(int)fsize];
                fis.read(bytes);
                fis.close();
            }
        } catch (IOException e) {
            System.err.println(MessageFormat.format("Error reading file {0}", path));
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public static boolean openReadManyClose(String path) {
        log("Doing O(nR)C cycles.");
        boolean result = true;
        try {
            File f = new File(path);
            long fsize = options.size;
            FileInputStream fis = new FileInputStream(f);
            for (int i = 0; i < options.reads; ++i) {
                bytes = new byte[(int)fsize];
                fis.read(bytes);
            }
            fis.close();
        } catch (IOException e) {
            System.err.println(MessageFormat.format("Error reading file {0}", path));
            e.printStackTrace();
            result = false;
        }
        return result;
    }
}
