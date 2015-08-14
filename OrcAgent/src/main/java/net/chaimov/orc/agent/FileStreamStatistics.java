package net.chaimov.orc.agent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by nchaimov on 8/12/15.
 */
public class FileStreamStatistics {
    private static LongAdder fileInputStreamOpenCalls = new LongAdder();
    private static LongAdder fileOutputStreamOpenCalls = new LongAdder();
    private static LongAdder fileInputStreamCloseCalls = new LongAdder();
    private static LongAdder fileOutputStreamCloseCalls = new LongAdder();
    private static LongAdder fileInputStreamOpenTime = new LongAdder();
    private static LongAdder fileOutputStreamOpenTime = new LongAdder();
    private static LongAdder fileInputStreamReadCalls = new LongAdder();
    private static LongAdder fileInputStreamReadTime = new LongAdder();
    private static LongAdder fileOutputStreamWriteCalls = new LongAdder();

    public static class PerFileStatistics {
        public LongAdder inputOpens = new LongAdder();
        public LongAdder inputCloses = new LongAdder();
        public LongAdder outputOpens = new LongAdder();
        public LongAdder outputCloses = new LongAdder();
        public LongAdder reads = new LongAdder();
        public LongAdder writes = new LongAdder();
        public LongAdder cumulativeInputOpenTime = new LongAdder();
        public LongAdder cumulativeOutputOpenTime = new LongAdder();
        public LongAdder cumulativeReadTime = new LongAdder();
        public LongAdder cumulativeWriteTime = new LongAdder();

        public static String getHeader() {
            return "inputOpens,inputCloses,outputOpens,outputCloses,inputOpenTime,outputOpenTime,reads,readTime";
        }

        public String toString() {
            return String.format("%d,%d,%d,%d,%d,%d,%d,%d", inputOpens.sum(), inputCloses.sum(),
                    outputOpens.sum(), outputCloses.sum(), cumulativeInputOpenTime.sum(),
                    cumulativeOutputOpenTime.sum(), reads.sum(), cumulativeReadTime.sum());
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
        fileInputStreamOpenCalls.increment();
        fileInputStreamOpenTime.add(time_ns);
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.inputOpens.increment();
        pf.cumulativeInputOpenTime.add(time_ns);
    }

    @SuppressWarnings( "unused" )
    public static void closedInputFile(String path, boolean closed) {
        if(closed) {
            return;
        }
        fileInputStreamCloseCalls.increment();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).inputCloses.increment();
    }

    @SuppressWarnings( "unused" )
    public static void openedOutputFile(String path, long time_ns) {
        fileOutputStreamOpenCalls.increment();
        fileOutputStreamOpenTime.add(time_ns);
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.outputOpens.increment();
        pf.cumulativeOutputOpenTime.add(time_ns);
    }

    @SuppressWarnings( "unused" )
    public static void closedOutputFile(String path, boolean closed) {
        if(closed) {
            return;
        }
        fileOutputStreamCloseCalls.increment();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).outputCloses.increment();
    }

    @SuppressWarnings( "unused" )
    public static void readInputFile(String path, long time_ns) {
        fileInputStreamReadCalls.increment();
        fileInputStreamReadTime.add(time_ns);
        path = ensureStatsExist(path);
        PerFileStatistics pf = perFileStatistics.get(path);
        pf.reads.increment();
        pf.cumulativeReadTime.add(time_ns);
    }

    public static String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("path,%s\n", PerFileStatistics.getHeader()));
        for (Map.Entry<String, PerFileStatistics> entry : perFileStatistics.entrySet()) {
            sb.append(String.format("%s,%s\n", entry.getKey(), entry.getValue().toString()));
        }
        sb.append(String.format("%s,%d,%d,%d,%d,%d,%d,%d,%d\n",
                "<TOTAL>",
                fileInputStreamOpenCalls.sum(),
                fileInputStreamCloseCalls.sum(),
                fileOutputStreamOpenCalls.sum(),
                fileOutputStreamCloseCalls.sum(),
                fileInputStreamOpenTime.sum(),
                fileOutputStreamOpenTime.sum(),
                fileInputStreamReadCalls.sum(),
                fileInputStreamReadTime.sum()));
        return sb.toString();
    }
}
