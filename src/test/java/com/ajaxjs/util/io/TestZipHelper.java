package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;

import java.io.File;

public class TestZipHelper {
//    @Test
//    public void testSpeicalFileZip() {
//        ZipHelper.zip("c:\\temp", "c:\\temp2\\test.zip", file -> {
//            System.out.println(file);
//            if ("c:\\temp\\foo".equals(file.toString()))
//                return false;
//
//            return true;
//        });
//    }

    @Test
    public void testFileUnZip() {
        ZipHelper.unzip("c:\\temp2\\", "c:\\temp2\\test.zip");
    }


    private static final String ZIP_PATH = "c:\\temp\\test.zip";

    @Test
    void testZipFile() {
        File file1 = new File("c:\\temp\\downloaded_image.jpg");
        File file2 = new File("c:\\temp\\readme.txt");

        ZipHelper.zipFile(new File[]{file1, file2}, ZIP_PATH, false);
    }

    @Test
    void testZipDir() {
        ZipHelper.zipDirectory("c:\\temp\\test", ZIP_PATH, false);
    }
}
