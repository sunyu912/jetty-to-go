package io.magnum.jetty.server.loadtest;

import io.magnum.jetty.server.data.TestInfo;
import io.magnum.jetty.server.data.provider.DataProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hd4ar.awscommon.AwsS3Helper;
import com.hd4ar.awscommon.exec.Exec;
import com.hd4ar.awscommon.retry.AbortException;

public class JMeterLoadTestManager implements LoadTestManager {

    private static final Logger logger = LoggerFactory.getLogger(JMeterLoadTestManager.class);
    private static final String TEST_BASE_FOLDER = "/tmp/roar-tests/";  
    private static final String S3_BUCKET = "roar-tests";
    private static String JMETER_EXEC;
    
    static {
        String jmeterBinFolder = System.getProperty("jmeter.bin", "/home/ubuntu/jmeter/bin");
        JMETER_EXEC = jmeterBinFolder + "/jmeter";
    }
    
    /** Executor service to handle jMeter execution */
    private ExecutorService executor;
    
    @Autowired
    private AwsS3Helper s3Helper;
    
    @Autowired
    private DataProvider dataProvider;
    
    public JMeterLoadTestManager() {
        executor = Executors.newFixedThreadPool(1);
    }
    
    @Override
    public String runTest(String testId, InputStream in) {
        // create test ID
        if (testId == null) {
            testId = UUID.randomUUID().toString();
        }
        
        // create file
        String testFolder = TEST_BASE_FOLDER + testId;
        File file = new File(testFolder, testId + ".jmx");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            OutputStream os = new FileOutputStream(file);
            IOUtils.copy(in, os);
            os.close();
            in.close();
        } catch (IOException e) {
            logger.error("Failed to save the input test spec file at {}", file.getAbsolutePath(), e);
        }             
        
        dataProvider.updateTestInfo(testId, TestInfo.PROCESSING);
        // execute
        executor.submit(new JMeterTestExecution(testId, testFolder, file.getAbsolutePath()));
        
        return testId;
    }

    class JMeterTestExecution implements Runnable {
        
        private static final int JMETER_TIMEOUT = 60 * 1000 * 10; // 10 mins
        private String testId;
        private String testFolder;
        private String inputFile;
        
        JMeterTestExecution(String testId, String testFolder, String testFile) {
            this.testId = testId;
            this.testFolder = testFolder;
            this.inputFile = testFile;
        }
        
        @Override
        public void run() {
            Exec exec = new Exec(
                    JMETER_EXEC 
                    + " -Dresult.dir=" + testFolder
                    + " -n -t " + inputFile
                    + " -l " + testFolder + "/records.csv"
                    + " -j " + testFolder + "/jmeter.log",
                    0, JMETER_TIMEOUT);
            try {
                // run jmeter
                exec.execute();
                // sync files to s3                
                s3Helper.syncLocalFilesToS3Public(testFolder, S3_BUCKET, testId);
                // update test status
                dataProvider.updateTestInfo(testId, TestInfo.COMPLETED);
            } catch (ExecuteException e) {
                logger.error("Failed to execute jmeter", e);
                dataProvider.updateTestInfo(testId, TestInfo.FAILED);
            } catch (IOException e) {
                logger.error("Failed to execute jmeter", e);
                dataProvider.updateTestInfo(testId, TestInfo.FAILED);
            } catch (AbortException e) {
                logger.error("Failed to sync files to S3", e);
                dataProvider.updateTestInfo(testId, TestInfo.FAILED);
            }
        }
    }
}
