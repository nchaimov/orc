package net.chaimov.orc.agent;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by nchaimov on 8/18/15.
 */
public class InstrumentedFileOutputStream extends OutputStream {


    private static KeyedObjectPool<File, FileOutputStream> regularPool =
            new GenericKeyedObjectPool<File, FileOutputStream>(new FileOutputStreamFactory());

    private static KeyedObjectPool<File, FileOutputStream> appendingPool =
            new GenericKeyedObjectPool<File, FileOutputStream>(new AppendingFileOutputStreamFactory());

    protected final FileOutputStream wrappedStream;
    protected final String path;
    protected final File myFile;
    protected boolean closed = false;
    protected final boolean isBorrowed;
    protected final boolean isAppending;

    public InstrumentedFileOutputStream(String name) throws FileNotFoundException {
        path = name;
        long start = System.nanoTime();
        myFile = new File(name);
        try {
            wrappedStream = regularPool.borrowObject(myFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileNotFoundException("Unable to borrow FileOutputStream from regular pool");
        }
        isBorrowed = true;
        isAppending = false;
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(name, end-start);
    }

    public InstrumentedFileOutputStream(String name, boolean append) throws FileNotFoundException {
        path = name;
        long start = System.nanoTime();
        myFile = new File(name);
        if(append) {
            try {
                wrappedStream = appendingPool.borrowObject(myFile);
                isAppending = true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new FileNotFoundException("Unable to borrow FileOutputStream from appending pool.");
            }
        } else {
            try {
                wrappedStream = regularPool.borrowObject(myFile);
                isAppending = false;
            } catch (Exception e) {
                e.printStackTrace();
                throw new FileNotFoundException("Unable to borrow FileOutputStream from regular pool.");
            }
        }
        isBorrowed = true;
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(name, end-start);
    }

    public InstrumentedFileOutputStream(File file) throws FileNotFoundException {
        path = (file != null ? file.getPath() : null);
        long start = System.nanoTime();
        myFile = file;
        try {
            wrappedStream = regularPool.borrowObject(myFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileNotFoundException("Unable to borrow FileOutputStream from regular pool");
        }
        isBorrowed = true;
        isAppending = false;
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(path, end-start);
    }

    public InstrumentedFileOutputStream(File file, boolean append) throws FileNotFoundException {
        path = (file != null ? file.getPath() : null);
        long start = System.nanoTime();
        myFile = file;
        if(append) {
            try {
                wrappedStream = appendingPool.borrowObject(myFile);
                isAppending = true;
            } catch (Exception e) {
                e.printStackTrace();
                throw new FileNotFoundException("Unable to borrow FileOutputStream from appending pool.");
            }
        } else {
            try {
                wrappedStream = regularPool.borrowObject(myFile);
                isAppending = false;
            } catch (Exception e) {
                e.printStackTrace();
                throw new FileNotFoundException("Unable to borrow FileOutputStream from regular pool.");
            }
        }
        isBorrowed = true;
        long end = System.nanoTime();
        FileStreamStatistics.openedOutputFile(path, end-start);
    }

    public InstrumentedFileOutputStream(FileDescriptor fdObj) {
        path = null;
        myFile = null;
        long start = System.nanoTime();
        wrappedStream = new FileOutputStream(fdObj);
        long end = System.nanoTime();
        isBorrowed = false;
        isAppending = false;
        FileStreamStatistics.openedOutputFile(path, end - start);
    }

    @Override
    public void write(int b) throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
        long start = System.nanoTime();
        wrappedStream.write(b);
        long end = System.nanoTime();
        FileStreamStatistics.writeOutputFile(path, end-start);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
        long start = System.nanoTime();
        wrappedStream.write(b);
        long end = System.nanoTime();
        FileStreamStatistics.writeOutputFile(path, end-start);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
        long start = System.nanoTime();
        wrappedStream.write(b, off, len);
        long end = System.nanoTime();
        FileStreamStatistics.writeOutputFile(path, end-start);
    }

    @Override
    public void close() throws IOException {
        if(!closed) {
            closed = true;
            if(isBorrowed) {
                if(isAppending) {
                    try {
                        appendingPool.returnObject(myFile, wrappedStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IOException("Unable to return FileOutputStream to appending pool");
                    }
                } else {
                    try {
                        regularPool.returnObject(myFile, wrappedStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IOException("Unable to return FileOutputStream to regular pool");
                    }
                }
            } else {
                wrappedStream.close();
            }
            FileStreamStatistics.closedOutputFile(path);
        }
    }

    public FileChannel getChannel() {
        return wrappedStream.getChannel();
    }

    @Override
    public void flush() throws IOException {
        if(closed) {
            throw new IOException("Use after close");
        }
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

    public static void closePool() throws Exception {
        regularPool.clear();
        appendingPool.clear();
        regularPool.close();
        appendingPool.close();
    }

}
