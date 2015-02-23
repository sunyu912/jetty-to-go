package io.magnum.jetty.server.url;

import java.io.File;

public class ResourceLocator {
    
    public static String tmpFolder = System.getProperty("java.io.tmpdir");
    
    public static final String IMAGE_BUCKET = "yu-tmp";
    public static final String URL_FILE_BUCKET = "bbds-url";
    public static final String S3URL_PREFIX = "https://s3.amazonaws.com";
    
    public static File getLocalImageFile(String id) {
        File f = new File(tmpFolder, id + ".png");
        return f;
    }
    
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }
}


