package net.chaimov.orc.agent;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;

public class FileInputStreamFactory extends BaseKeyedPooledObjectFactory<File, FileInputStream>{

    @Override
    public FileInputStream create(File file) throws Exception {
        FileInputStream f = new FileInputStream(file);
        FileStreamStatistics.actuallyOpenedInputFile(file.getPath());
        return f;
    }

    @Override
    public void destroyObject(File key, PooledObject<FileInputStream> p) throws Exception {
        FileInputStream fileInputStream = p.getObject();
        try {
            Field pathField = FileInputStream.class.getDeclaredField("path");
            pathField.setAccessible(true);
            String path = (String) pathField.get(fileInputStream);
            FileStreamStatistics.actuallyClosedInputFile(path);
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
    public PooledObject<FileInputStream> wrap(FileInputStream fileInputStream) {
        PooledObject<FileInputStream> o = new DefaultPooledObject<FileInputStream>(fileInputStream);
        return o;
    }

    @Override
    public void passivateObject(File key, PooledObject<FileInputStream> p) throws Exception {
        super.passivateObject(key, p);
        p.getObject().getChannel().position(0L); // Go back to beginning of file.
    }
}
