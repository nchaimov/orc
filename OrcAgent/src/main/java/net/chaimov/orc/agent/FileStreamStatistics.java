package net.chaimov.orc.agent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by nchaimov on 8/12/15.
 */
public class FileStreamStatistics {
    private static AtomicLong fileInputStreamOpenCalls = new AtomicLong(0L);
    private static AtomicLong fileOutputStreamOpenCalls = new AtomicLong(0L);
    private static AtomicLong fileInputStreamCloseCalls = new AtomicLong(0L);
    private static AtomicLong fileOutputStreamCloseCalls = new AtomicLong(0L);

    public static class PerFileStatistics {
        public AtomicLong inputOpens = new AtomicLong(0L);
        public AtomicLong inputCloses = new AtomicLong(0L);
        public AtomicLong outputOpens = new AtomicLong(0L);
        public AtomicLong outputCloses = new AtomicLong(0L);

        public String toString() {
            return String.format("%-10d %-10d %-10d %-10d", inputOpens.get(), inputCloses.get(),
                    outputOpens.get(), outputCloses.get());
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

    public static void openedInputFile(String path) {
        fileInputStreamOpenCalls.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).inputOpens.incrementAndGet();
    }

    public static void closedInputFile(String path, boolean closed) {
        if(closed) {
            return;
        }
        fileInputStreamCloseCalls.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).inputCloses.incrementAndGet();
    }

    public static void openedOutputFile(String path) {
        fileOutputStreamOpenCalls.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).outputOpens.incrementAndGet();
    }

    public static void closedOutputFile(String path, boolean closed) {
        if(closed) {
            return;
        }
        fileOutputStreamCloseCalls.incrementAndGet();
        path = ensureStatsExist(path);
        perFileStatistics.get(path).outputCloses.incrementAndGet();
    }

    public static String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-128s\t%-10s %-10s %-10s %-10s\n", "path", "iOpen", "iClose", "oOpen", "oClose"));
        for (Map.Entry<String, PerFileStatistics> entry : perFileStatistics.entrySet()) {
            sb.append(String.format("%-128s\t%-40s\n", entry.getKey(), entry.getValue().toString()));
        }
        sb.append(String.format("\n%-128s\t%-10s %-10s %-10s %-10s\n",
                "TOTAL",
                fileInputStreamOpenCalls.get(),
                fileInputStreamCloseCalls.get(),
                fileOutputStreamOpenCalls.get(),
                fileOutputStreamCloseCalls.get()));
        return sb.toString();
    }
}
