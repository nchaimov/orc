package net.chaimov.orc.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by nchaimov on 8/7/15.
 */
public class OrcClient {

    public static final int iterations = 100;
   public static void main(String[] args) throws IOException {
       System.out.println("This is OrcClient.");

       final List<Thread> threads = new LinkedList<Thread>();
       final LinkedList<String> files = new LinkedList<String>();

       for(int i = 0; i < iterations; ++i) {
           final int finalI = i;
           final String outname = "bar_" + finalI;
           files.add(outname);
           Thread t = new Thread(new Runnable() {
               public void run() {
                   try {
                       FileInputStream fis = new FileInputStream("foo");
                       long len = fis.getChannel().size();
                       byte[] contents = new byte[(int) len];
                       fis.read(contents);
                       fis.close();
                       FileOutputStream fos = new FileOutputStream(outname);
                       fos.write(contents);
                       fos.close();
                   } catch(Exception e) {
                       e.printStackTrace();
                   }
               }
           });
           threads.add(t);
       }

       for(Thread t : threads) {
           t.start();
       }

       for(Thread t : threads) {
           try {
               t.join();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }

       for(String s : files) {
           File f = new File(s);
           if(f.exists()) {
               f.delete();
           }
       }

   }
}
