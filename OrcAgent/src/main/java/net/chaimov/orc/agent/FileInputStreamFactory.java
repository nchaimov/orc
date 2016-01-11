package net.chaimov.orc.agent;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by nchaimov on 1/11/16.
 */
public class FileInputStreamFactory extends BaseKeyedPooledObjectFactory<File, FileInputStream>{

    @Override
    public FileInputStream create(File file) throws Exception {
        FileInputStream f = new FileInputStream(file);
        return f;
    }

    @Override
    public PooledObject<FileInputStream> wrap(FileInputStream fileInputStream) {
        PooledObject<FileInputStream> o = new DefaultPooledObject<FileInputStream>(fileInputStream);
        return o;
    }
}
