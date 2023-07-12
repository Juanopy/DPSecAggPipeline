package com.example.database_test.gmsreadout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

    public static void copyDir(File source, File target)
            throws IOException {

        if (!source.isDirectory()) {
            copyFile(source, target);

        } else {
            if (!target.exists()) {
                target.mkdirs();
            }

            String[] Subfolder = source.list();
            for (int a = 0; a < Subfolder.length; a++) {
                copyDir(new File(source, Subfolder[a]), new File(target, Subfolder[a]));
            }
        }
    }


    public static void copyFile(File source, File target)
            throws IOException {
        int len;
        byte[] buf = new byte[1024];
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
