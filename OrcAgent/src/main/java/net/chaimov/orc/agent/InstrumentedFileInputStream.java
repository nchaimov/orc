package net.chaimov.orc.agent;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by nchaimov on 8/18/15.
 */
public class InstrumentedFileInputStream extends InputStream {

    private static KeyedObjectPool<File, FileInputStream> pool =
            new GenericKeyedObjectPool<File, FileInputStream>(new FileInputStreamFactory());

    protected final FileInputStream wrappedStream;
    protected final boolean isBorrowed;
    protected final String path;
    protected final File myFile;
    protected boolean closed = false;

    public InstrumentedFileInputStream(String name) throws FileNotFoundException {
        path = name;
        long start = System.nanoTime();
        myFile = new File(name);
        try {
            wrappedStream = pool.borrowObject(myFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileNotFoundException("Unable to borrow FileInputStream from pool");
        }
        isBorrowed = true;
        long end = System.nanoTime();
        FileStreamStatistics.openedInputFile(path, end-start);
    }

    public InstrumentedFileInputStream(File file) throws FileNotFoundException {
        path = (file != null ? file.getPath() : null);
        long start = System.nanoTime();
        myFile = file;
        try {
            wrappedStream = pool.borrowObject(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileNotFoundException();
        }
        isBorrowed = true;
        long end = System.nanoTime();
        FileStreamStatistics.openedInputFile(path, end-start);
    }

    public InstrumentedFileInputStream(FileDescriptor fdObj) {
        path = null;
        myFile = null;
        long start = System.nanoTime();
        wrappedStream = new FileInputStream(fdObj);
        long end = System.nanoTime();
        FileStreamStatistics.openedInputFile(path, end-start);
        isBorrowed = false;
    }

    @Override
    public int read() throws IOException {
        if(closed) {
            throw new IOException("Use after close.");
        }
        long start = System.nanoTime();
        int result =  wrappedStream.read();
        long end = System.nanoTime();
        FileStreamStatistics.readInputFile(path, end - start);
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
        long start = System.nanoTime();
        int result = wrappedStream.read(b);
        long end = System.nanoTime();
        FileStreamStatistics.readInputFile(path, end - start);
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
        long start = System.nanoTime();
        int result =  wrappedStream.read(b, off, len);
        long end = System.nanoTime();
        FileStreamStatistics.readInputFile(path, end - start);
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
        return wrappedStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
        return wrappedStream.available();
    }

    @Override
    public void close() throws IOException {
        if(!closed) {
            closed = true;
            if(isBorrowed) {
                try {
                    pool.returnObject(myFile, wrappedStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IOException("Failed to return FileInputStream to pool");
                }
            } else {
                wrappedStream.close();
            }
            FileStreamStatistics.closedInputFile(path);
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        wrappedStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
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

    public static void closePool() throws Exception {
        pool.clear();
        pool.close();
    }
}
