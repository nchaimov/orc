package net.chaimov.orc.agent;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by nchaimov on 8/18/15.
 */
public class InstrumentedFileInputStream extends InputStream {

    protected final FileInputStream wrappedStream;
    protected final String path;
    protected boolean closed = false;

    public InstrumentedFileInputStream(String name) throws FileNotFoundException {
        path = name;
        long start = System.nanoTime();
        wrappedStream = new FileInputStream(name);
        long end = System.nanoTime();
        FileStreamStatistics.openedInputFile(path, end-start);
    }

    public InstrumentedFileInputStream(File file) throws FileNotFoundException {
        path = (file != null ? file.getPath() : null);
        long start = System.nanoTime();
        wrappedStream = new FileInputStream(file);
        long end = System.nanoTime();
        FileStreamStatistics.openedInputFile(path, end-start);
    }

    public InstrumentedFileInputStream(FileDescriptor fdObj) {
        path = null;
        long start = System.nanoTime();
        wrappedStream = new FileInputStream(fdObj);
        long end = System.nanoTime();
        FileStreamStatistics.openedInputFile(path, end-start);
    }

    @Override
    public int read() throws IOException {
        long start = System.nanoTime();
        int result =  wrappedStream.read();
        long end = System.nanoTime();
        FileStreamStatistics.readInputFile(path, end - start);
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        long start = System.nanoTime();
        int result = wrappedStream.read(b);
        long end = System.nanoTime();
        FileStreamStatistics.readInputFile(path, end - start);
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long start = System.nanoTime();
        int result =  wrappedStream.read(b, off, len);
        long end = System.nanoTime();
        FileStreamStatistics.readInputFile(path, end - start);
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return wrappedStream.available();
    }

    @Override
    public void close() throws IOException {
        if(!closed) {
            closed = true;
            wrappedStream.close();
            FileStreamStatistics.closedInputFile(path);
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        wrappedStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrappedStream.reset();
    }

    @Override
    public boolean markSupported() {
        return wrappedStream.markSupported();
    }

    public FileChannel getChannel() {
        return wrappedStream.getChannel();
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
