package net.chaimov.orc.agent;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

public class FileOutputStreamFactory extends BaseKeyedPooledObjectFactory<File, FileOutputStream> {

    @Override
    public FileOutputStream create(File file) throws Exception {
        FileOutputStream f = new FileOutputStream(file, false);
        FileStreamStatistics.actuallyOpenedOutputFile(file.getPath());
        return f;
    }

    @Override
    public void destroyObject(File key, PooledObject<FileOutputStream> p) throws Exception {
        FileOutputStream fileOutputStream = p.getObject();
        try {
            Field pathField = FileOutputStream.class.getDeclaredField("path");
            pathField.setAccessible(true);
            String path = (String) pathField.get(fileOutputStream);
            FileStreamStatistics.actuallyClosedOutputFile(path);
        } catch (NoSuchFieldException e) {
            // Ignore
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // Ignore
            e.printStackTrace();
        }
        p.getObject().close();
        super.destroyObject(key, p);
    }

    @Override
    public PooledObject<FileOutputStream> wrap(FileOutputStream fileOutputStream) {
        PooledObject<FileOutputStream> o = new DefaultPooledObject<FileOutputStream>(fileOutputStream);
        return o;
    }

    @Override
    public void passivateObject(File key, PooledObject<FileOutputStream> p) throws Exception {
        super.passivateObject(key, p);
        p.getObject().flush();
        p.getObject().getChannel().position(0L); // Go back to beginning of file.
    }

}
