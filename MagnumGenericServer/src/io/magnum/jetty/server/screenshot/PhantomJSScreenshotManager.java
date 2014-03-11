package io.magnum.jetty.server.screenshot;

import io.magnum.jetty.server.data.ScreenshotRecord;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hd4ar.awscommon.AwsS3Helper;
import com.hd4ar.awscommon.exec.Exec;
import com.hd4ar.awscommon.retry.AbortException;

public class PhantomJSScreenshotManager implements ScreenshotManager {
    
    private static Logger logger = LoggerFactory.getLogger(PhantomJSScreenshotManager.class);
    
    private static final String IMAGE_BUCKET = "yu-test";
    private static final String S3URL_PREFIX = "https://s3.amazonaws.com";

    @Autowired
    private AwsS3Helper s3Helper;
    
    @Override
    public ScreenshotRecord getScreenshot(String url) {
        Long timestamp = System.currentTimeMillis();
        File tmpImageFile = null;
        try {
            tmpImageFile = File.createTempFile(UUID.randomUUID().toString(), ".png");
        } catch (IOException e) {
            logger.error("Failed to create the tmp file for screenshot", e);
            return null;
        }
        
        // capture screenshot
        Exec exec = new Exec("/usr/local/bin/phantomjs /usr/local/Cellar/phantomjs/1.9.7/share/phantomjs/examples/rasterize.js " 
                + url + " " + tmpImageFile.getAbsolutePath());
        try {
            logger.info("Capturing screenshot for url {}", url);
            exec.execute();
            
            logger.info("Uploading the file {} to S3", tmpImageFile.getAbsolutePath());
            s3Helper.uploadFileToS3(tmpImageFile.getAbsolutePath(), IMAGE_BUCKET, tmpImageFile.getName(), true);
            
            logger.info("Deleting the file {}", tmpImageFile.getAbsolutePath());
            tmpImageFile.delete();
        } catch (IOException e) {
            logger.error("Failed to exec the screenshot command for url {}", url, e);
        } catch (AbortException e) {
            logger.error("Failed to upload the screenshot file {} to S3 {} {}", 
                    tmpImageFile.getAbsolutePath(), IMAGE_BUCKET, tmpImageFile.getName(), e);
        }
                
        ScreenshotRecord record = new ScreenshotRecord();
        record.setTimestamp(timestamp);
        record.setUrl(url);
        record.setImageS3Url(S3URL_PREFIX + "/" + IMAGE_BUCKET + "/" + tmpImageFile.getName());
        
        return record;
    }
}
