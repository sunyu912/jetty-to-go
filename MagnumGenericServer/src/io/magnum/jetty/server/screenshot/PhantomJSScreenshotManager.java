package io.magnum.jetty.server.screenshot;

import io.magnum.jetty.server.data.ScreenshotRecord;
import io.magnum.jetty.server.url.ResourceLocator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hd4ar.awscommon.AwsS3Helper;
import com.hd4ar.awscommon.exec.Exec;
import com.hd4ar.awscommon.retry.AbortException;

public class PhantomJSScreenshotManager implements ScreenshotManager {
    
    private static Logger logger = LoggerFactory.getLogger(PhantomJSScreenshotManager.class);
    
    @Autowired
    private AwsS3Helper s3Helper;
    
    @Override
    public ScreenshotRecord getScreenshot(String inputUrl) {
        // normalize URL
        inputUrl = inputUrl.trim();
        List<String> urlRetyList = new ArrayList<String>();
        if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {            
            urlRetyList.add("http://" + inputUrl);
            if (!inputUrl.startsWith("www")) {
                urlRetyList.add("http://www." + inputUrl);
            }
            urlRetyList.add("https://" + inputUrl);            
        } else {
            urlRetyList.add(inputUrl);
        }
                
        Long timestamp = System.currentTimeMillis();
        
        ScreenshotRecord record = new ScreenshotRecord();
        record.setTimestamp(timestamp);
        record.setUrl(urlRetyList.get(0));
                
        File tmpImageFile = null;
        try {
            tmpImageFile = File.createTempFile(UUID.randomUUID().toString(), ".png");
        } catch (IOException e) {
            logger.error("Failed to create the tmp file for screenshot", e);
            record.setSuccess(false);
            return record;
        }
                
        try {    
            boolean isSuccess = false;
            for(String url : urlRetyList) {
                logger.info("Capturing screenshot for url {}", url);
                // capture screenshot
                Exec exec = new Exec("/usr/local/bin/phantomjs /usr/local/Cellar/phantomjs/1.9.7/share/phantomjs/examples/rasterize.js "
                        + url + " " + tmpImageFile.getAbsolutePath());
//                Exec exec = new Exec("/home/ubuntu/phantomjs/bin/phantomjs /home/ubuntu/rasterize.js "
//                        + url + " " + tmpImageFile.getAbsolutePath());
                
                try {
                    exec.execute();
                } catch (Exception e) {
                    logger.error("Failed to exec the screenshot command for url {}", url, e);        
                }
                
                // check if it is success
                if (tmpImageFile.length() != 0) {
                    isSuccess = true;
                    record.setUrl(url);
                    record.setSuccess(true);
                    logger.info("Capturing screenshot for url {} successfully", url);
                    break;
                }
            }
            
            if (isSuccess) {
                logger.info("Uploading the file {} to S3", tmpImageFile.getAbsolutePath());
                s3Helper.uploadFileToS3(tmpImageFile.getAbsolutePath(), ResourceLocator.IMAGE_BUCKET, tmpImageFile.getName(), true);                
                record.setImageS3Url(ResourceLocator.S3URL_PREFIX + "/" + ResourceLocator.IMAGE_BUCKET + "/" + tmpImageFile.getName());
            }
            
            logger.info("Deleting the file {}", tmpImageFile.getAbsolutePath());
            tmpImageFile.delete();
        } catch (AbortException e) {
            logger.error("Failed to upload the screenshot file {} to S3 {} {}", 
                    tmpImageFile.getAbsolutePath(), ResourceLocator.IMAGE_BUCKET, tmpImageFile.getName(), e);
        }
        
        return record;
    }
}
