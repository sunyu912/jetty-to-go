package io.magnum.jetty.server.screenshot;

import io.magnum.jetty.server.analyzer.VizwebAnalyzer;
import io.magnum.jetty.server.data.AnalysisResult;
import io.magnum.jetty.server.data.BatchRunHistoryRecord;
import io.magnum.jetty.server.data.BatchRunRecord;
import io.magnum.jetty.server.data.BatchRunResultRecord;
import io.magnum.jetty.server.data.ColorFeatureResult;
import io.magnum.jetty.server.data.QuadtreeFeatureResult;
import io.magnum.jetty.server.data.ScreenshotRecord;
import io.magnum.jetty.server.data.XYFeatureResult;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.url.ResourceLocator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hd4ar.awscommon.AwsS3Helper;
import com.hd4ar.awscommon.exec.Exec;
import com.hd4ar.awscommon.retry.AbortException;

public class PhantomJSScreenshotManager implements ScreenshotManager {
    
    private static Logger logger = LoggerFactory.getLogger(PhantomJSScreenshotManager.class);
    private static final String TEST_BASE_FOLDER = "/tmp/url-files/"; 
    
    @Autowired
    private AwsS3Helper s3Helper;
    
    @Autowired
    private DataProvider dataProvider;
    
    @Autowired
    private VizwebAnalyzer vizwebAnalyzer;
    
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    
    @Override
    public ScreenshotRecord getScreenshot(String inputUrl, boolean enableAnalyze) {
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
        String id = UUID.randomUUID().toString();        
        try {
            tmpImageFile = File.createTempFile(id, ".png");
            record.setId(tmpImageFile.getName().substring(0, tmpImageFile.getName().length() - 4));
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
//                Exec exec = new Exec("/usr/local/bin/phantomjs /usr/local/Cellar/phantomjs/1.9.7/share/phantomjs/examples/rasterize.js "
//                        + url + " " + tmpImageFile.getAbsolutePath());
                Exec exec = new Exec("/home/ubuntu/phantomjs/bin/phantomjs /home/ubuntu/rasterize.js "
                        + url + " " + tmpImageFile.getAbsolutePath());
                
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
                
                if (enableAnalyze) {
                    logger.info("Start analysis process");
                    ColorFeatureResult colorResult = vizwebAnalyzer.computeColorFeature(id, tmpImageFile);
                    XYFeatureResult xyResult = vizwebAnalyzer.computerXYFeature(id, tmpImageFile);
                    QuadtreeFeatureResult quadResult = vizwebAnalyzer.computerQuadtreeFeature(id, tmpImageFile);
                    AnalysisResult result = new AnalysisResult();
                    result.setColorResult(colorResult);
                    result.setQuadResult(quadResult);
                    result.setXyResult(xyResult);
                    record.setAnalysisResult(result);
                    logger.info("Finished analysis process");
                }
                
            }
            
            logger.info("Deleting the file {}", tmpImageFile.getAbsolutePath());
            tmpImageFile.delete();
        } catch (AbortException e) {
            logger.error("Failed to upload the screenshot file {} to S3 {} {}", 
                    tmpImageFile.getAbsolutePath(), ResourceLocator.IMAGE_BUCKET, tmpImageFile.getName(), e);
        }
        
        return record;
    }

    @Override
    public String addBatchRun(String description, InputStream in) {     
        
        String batchId = UUID.randomUUID().toString();
        
        // create file
        String testFolder = TEST_BASE_FOLDER;
        File file = new File(testFolder, batchId + ".txt");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // save content
        try {
            OutputStream os = new FileOutputStream(file);
            IOUtils.copy(in, os);
            os.close();
            in.close();
            // save to S3
            logger.info("Uploading the file {} to S3", file.getAbsolutePath());
            s3Helper.uploadFileToS3(file.getAbsolutePath(), ResourceLocator.URL_FILE_BUCKET, 
                    file.getName(), true);
            
            // set record
            String url = ResourceLocator.S3URL_PREFIX + "/" + ResourceLocator.URL_FILE_BUCKET 
                    + "/" + file.getName();
            BatchRunRecord r = new BatchRunRecord();
            r.setFileUrl(url);
            r.setDescription(description);
            r.setId(batchId);
            dataProvider.addGenericObject(r);            
        } catch (IOException e) {
            logger.error("Failed to save the url list file at {}", file.getAbsolutePath(), e);
        } catch (AbortException e) {
            logger.error("Failed to save the url list file to S3", e);
        }        
        
        return batchId;
    }

    @Override
    public BatchRunHistoryRecord batchRun(String id) {
        String url = ResourceLocator.S3URL_PREFIX + "/" + ResourceLocator.URL_FILE_BUCKET + "/" + id + ".txt";
        BatchRunHistoryRecord record = new BatchRunHistoryRecord();
        record.setId(id);
        record.setTimestamp(System.currentTimeMillis());
        record.setCompleted(false);
        try {
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream("/tmp/" + id + ".txt");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            Scanner s = new Scanner(new File("/tmp/" + id + ".txt"));
            List<String> urlList = new ArrayList<String>();
            while(s.hasNext()) {
                urlList.add(s.nextLine());
            }
            // submit and run
            executor.submit(new BatchRunTask(record, urlList));
        } catch (Exception e) {
            logger.error("Failed to get the URL file", e);
        }
        dataProvider.addGenericObject(record);
        return record;
    }
    
    private void batchRunTrigger(BatchRunHistoryRecord historyRecord, List<String> urlList) {
        for(int i = 0; i < urlList.size(); i++) {
            ScreenshotRecord record = getScreenshot(urlList.get(i), true);
            BatchRunResultRecord r = new BatchRunResultRecord();
            r.setResultRecord(record);
            r.setSequence((long) i);
            r.setTimestamp(historyRecord.getTimestamp());
            dataProvider.addGenericObject(r);
        }
        historyRecord.setCompleted(true);
        dataProvider.addGenericObject(historyRecord);
    }       
    
    private class BatchRunTask implements Runnable {
        
        private BatchRunHistoryRecord record;
        private List<String> urlList;
        
        BatchRunTask(BatchRunHistoryRecord record, List<String> urlList) {
            this.record = record;
            this.urlList = urlList;
        }
        
        @Override
        public void run() {
            batchRunTrigger(record, urlList);
        }        
    }
}
