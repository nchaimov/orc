package net.chaimov.orc.agent;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by nchaimov on 8/18/15.
 */
public class InstrumentedFileOutputStream extends OutputStream {

    protected final FileOutputStream wrappedStream;
    protected final String path;
    protected boolean closed = false;

    public InstrumentedFileOutputStream(String name) throws FileNotFoundException {
        path = name;
        long start = System.nanoTime();
        wrappedStream = new FileOutputStream(name);
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(name, end-start);
    }

    public InstrumentedFileOutputStream(String name, boolean append) throws FileNotFoundException {
        path = name;
        long start = System.nanoTime();
        wrappedStream = new FileOutputStream(name, append);
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(name, end-start);
    }

    public InstrumentedFileOutputStream(File file) throws FileNotFoundException {
        path = (file != null ? file.getPath() : null);
        long start = System.nanoTime();
        wrappedStream = new FileOutputStream(file);
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(path, end-start);
    }

    public InstrumentedFileOutputStream(File file, boolean append) throws FileNotFoundException {
        path = (file != null ? file.getPath() : null);
        long start = System.nanoTime();
        wrappedStream = new FileOutputStream(file, append);
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(path, end-start);
    }

    public InstrumentedFileOutputStream(FileDescriptor fdObj) {
        path = null;
        long start = System.nanoTime();
        wrappedStream = new FileOutputStream(fdObj);
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(path, end - start);
    }

    @Override
    public void write(int b) throws IOException {
        long start = System.nanoTime();
        wrappedStream.write(b);
        long end = System.nanoTime();
        FileStreamStatistics.writeOutputFile(path, end-start);
    }

    @Override
    public void write(byte[] b) throws IOException {
        long start = System.nanoTime();
        wrappedStream.write(b);
        long end = System.nanoTime();
        FileStreamStatistics.writeOutputFile(path, end-start);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        long start = System.nanoTime();
        wrappedStream.write(b, off, len);
        long end = System.nanoTime();
        FileStreamStatistics.writeOutputFile(path, end-start);
    }

    @Override
    public void close() throws IOException {
        if(!closed) {
            closed = true;
            wrappedStream.close();
            FileStreamStatistics.closedOutputFile(path);
        }
    }

    public FileChannel getChannel() {
        return wrappedStream.getChannel();
    }

    @Override
    public void flush() throws IOException {
        wrappedStream.flush();
    }

    @Override
    public int hashCode() {
        return wrappedStream.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return wrappedStream.equals(obj);
    }

    @Override
    public String toString() {
        return wrappedStream.toString();
    }

}
