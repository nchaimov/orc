package net.chaimov.orc.agent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by nchaimov on 8/12/15.
 */
public class FileStreamStatistics {
    private static AtomicLong fileInputStreamOpenCalls = new AtomicLong();
    private static AtomicLong fileOutputStreamOpenCalls = new AtomicLong();
    private static AtomicLong fileInputStreamCloseCalls = new AtomicLong();
    private static AtomicLong fileOutputStreamCloseCalls = new AtomicLong();
    private static AtomicLong fileInputStreamActualOpens = new AtomicLong();
    private static AtomicLong fileOutputStreamActualOpens = new AtomicLong();
    private static AtomicLong fileInputStreamActualCloses = new AtomicLong();
    private static AtomicLong fileOutputStreamActualCloses = new AtomicLong();
    private static AtomicLong fileInputStreamOpenTime = new AtomicLong();
    private static AtomicLong fileOutputStreamOpenTime = new AtomicLong();
    private static AtomicLong fileInputStreamReadCalls = new AtomicLong();
    private static AtomicLong fileInputStreamReadTime = new AtomicLong();
    private static AtomicLong fileOutputStreamWriteCalls = new AtomicLong();
    private static AtomicLong fileOutputStreamWriteTime = new AtomicLong();

    public static class PerFileStatistics {
        public AtomicLong inputOpens = new AtomicLong();
        public AtomicLong inputCloses = new AtomicLong();
        public AtomicLong outputOpens = new AtomicLong();
        public AtomicLong outputCloses = new AtomicLong();
        public AtomicLong inputActualOpens = new AtomicLong();
        public AtomicLong inputActualCloses = new AtomicLong();
        public AtomicLong outputActualOpens = new AtomicLong();
        public AtomicLong outputActualCloses = new AtomicLong();
        public AtomicLong reads = new AtomicLong();
        public AtomicLong writes = new AtomicLong();
        public AtomicLong cumulativeInputOpenTime = new AtomicLong();
        public AtomicLong cumulativeOutputOpenTime = new AtomicLong();
        public AtomicLong cumulativeReadTime = new AtomicLong();
        public AtomicLong cumulativeWriteTime = new AtomicLong();

        public static String getHeader() {
            return "inputOpens,inputCloses,outputOpens,outputCloses,inputActualOpens,inputActualCloses,outputActualOpens,outputActualCloses,inputOpenTime,outputOpenTime,reads,readTime,writes,writeTime";
        }

        public String toString() {
            return String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d", inputOpens.get(), inputCloses.get(),
                    outputOpens.get(), outputCloses.get(), inputActualOpens.get(), inputActualCloses.get(),
                    outputActualOpens.get(), outputActualCloses.get(), cumulativeInputOpenTime.get(),
                    cumulativeOutputOpenTime.get(), reads.get(), cumulativeReadTime.get(),
                    writes.get(), cumulativeWriteTime.get());
        }
    }

    private static ConcurrentMap<String, PerFileStatistics> perFileStatistics =
            new ConcurrentHashMap<String, PerFileStatistics>();


    private static String ensureStatsExist(String path) {
        if (path == null) {
            path = "<NO FILENAME>";
        }
        perFileStatistics.putIfAbsent(path, new PerFileStatistics());
        return path;
    }

    // Unused warning is suppressed on these methods because they are only used in injected code.

    @SuppressWarnings( "unused" )
    public static void openedInputFile(String path, long time_ns) {
        fileInputStreamOpenCalls.incrementAndGet();
        fileInputStreamOpenTime.addAndGet(time_ns);
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.inputOpens.incrementAndGet();
        pf.cumulativeInputOpenTime.addAndGet(time_ns);
    }

    @SuppressWarnings( "unused" )
    public static void actuallyOpenedInputFile(String path) {
        fileInputStreamActualOpens.incrementAndGet();
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.inputActualOpens.incrementAndGet();
    }

    @SuppressWarnings( "unused" )
    public static void closedInputFile(String path) {
        closedInputFile(path, false);
    }

    @SuppressWarnings( "unused" )
    public static void closedInputFile(String path, boolean closed) {
        if(closed) {
            return;
        }
        fileInputStreamCloseCalls.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).inputCloses.incrementAndGet();
    }

    @SuppressWarnings( "unused" )
    public static void actuallyClosedInputFile(String path) {
        fileInputStreamActualCloses.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).inputActualCloses.incrementAndGet();
    }

    @SuppressWarnings( "unused" )
    public static void openedOutputFile(String path, long time_ns) {
        fileOutputStreamOpenCalls.incrementAndGet();
        fileOutputStreamOpenTime.addAndGet(time_ns);
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.outputOpens.incrementAndGet();
        pf.cumulativeOutputOpenTime.addAndGet(time_ns);
    }

    @SuppressWarnings( "unused" )
    public static void actuallyOpenedOutputFile(String path) {
        fileOutputStreamActualOpens.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).outputActualOpens.incrementAndGet();
    }

    @SuppressWarnings( "unused" )
    public static void closedOutputFile(String path) {
        closedOutputFile(path, false);
    }

    @SuppressWarnings( "unused" )
    public static void closedOutputFile(String path, boolean closed) {
        if(closed) {
            return;
        }
        fileOutputStreamCloseCalls.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).outputCloses.incrementAndGet();
    }

    @SuppressWarnings( "unused" )
    public static void actuallyClosedOutputFile(String path) {
        fileOutputStreamActualCloses.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).outputActualCloses.incrementAndGet();
    }

    @SuppressWarnings( "unused" )
    public static void readInputFile(String path, long time_ns) {
        fileInputStreamReadCalls.incrementAndGet();
        fileInputStreamReadTime.addAndGet(time_ns);
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.reads.incrementAndGet();
        pf.cumulativeReadTime.addAndGet(time_ns);
    }

    @SuppressWarnings( "unused" )
    public static void writeOutputFile(String path, long time_ns) {
        fileOutputStreamWriteCalls.incrementAndGet();
        fileOutputStreamWriteTime.addAndGet(time_ns);
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.writes.incrementAndGet();
        pf.cumulativeWriteTime.addAndGet(time_ns);
    }

    public static String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("path,%s\n", PerFileStatistics.getHeader()));
        for (Map.Entry<String, PerFileStatistics> entry : perFileStatistics.entrySet()) {
            sb.append(String.format("\"%s\",%s\n", entry.getKey(), entry.getValue().toString()));
        }
        sb.append(String.format("\"%s\",%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                "<TOTAL>",
                fileInputStreamOpenCalls.get(),
                fileInputStreamCloseCalls.get(),
                fileOutputStreamOpenCalls.get(),
                fileOutputStreamCloseCalls.get(),
                fileInputStreamActualOpens.get(),
                fileInputStreamActualCloses.get(),
                fileOutputStreamActualOpens.get(),
                fileOutputStreamActualCloses.get(),
                fileInputStreamOpenTime.get(),
                fileOutputStreamOpenTime.get(),
                fileInputStreamReadCalls.get(),
                fileInputStreamReadTime.get(),
                fileOutputStreamWriteCalls.get(),
                fileOutputStreamWriteTime.get()));
        return sb.toString();
    }
}
