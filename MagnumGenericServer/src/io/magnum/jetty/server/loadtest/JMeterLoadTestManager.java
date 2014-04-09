package io.magnum.jetty.server.loadtest;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.TestInfo;
import io.magnum.jetty.server.data.analysis.TestDataPostProcesser;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.data.shared.GlobalDataCollectorJsonWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hd4ar.awscommon.AwsS3Helper;
import com.hd4ar.awscommon.exec.Exec;
import com.hd4ar.awscommon.retry.AbortException;

public class JMeterLoadTestManager implements LoadTestManager {

    private static final Logger logger = LoggerFactory.getLogger(JMeterLoadTestManager.class);
    private static final ObjectMapper mapper = new ObjectMapper() {{
        configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }};
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
    public String runTest(String testId, InputStream in, String containerId, String instanceType) {
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
        executor.submit(new JMeterTestExecution(testId, testFolder, file.getAbsolutePath(), containerId, instanceType));
        
        return testId;
    }
    
    @Override
    public void postProcessingData(String testId, String containerId, String instanceType) {
        try {
            URL url = new URL("https://s3.amazonaws.com/roar-tests/" + testId + "/throughput-perm.json");
            GlobalDataCollectorJsonWrapper data = mapper.readValue(url, GlobalDataCollectorJsonWrapper.class);
            TestDataPostProcesser processor = new TestDataPostProcesser(data);
            String testFolder = TEST_BASE_FOLDER + testId;
            File file = new File(testFolder, "throughput-perm-processed.json");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            mapper.writeValue(file, processor.getData());
            // sync back
            s3Helper.syncLocalFilesToS3Public(testFolder, S3_BUCKET, testId);
            // save record in Dynamo
            logger.info("Updating database for {} / {}", containerId, instanceType);
            if (containerId != null && instanceType != null) {
                AppPerformanceRecord record = new AppPerformanceRecord();
                record.setContainerId(containerId);
                record.setInstanceType(instanceType);
                record.setThroughputList(processor.getCleanedList());
                dataProvider.updateGeneric(record);
            }
        } catch (IOException e) {
            logger.error("Failed to load the json file", e);
        } catch (AbortException e) {
            logger.error("Failed to sync the perf file back to s3", e);
        }        
    }
    
    

    class JMeterTestExecution implements Runnable {
        
        private static final int JMETER_TIMEOUT = 60 * 1000 * 10; // 10 mins
        private String testId;
        private String testFolder;
        private String inputFile;
        private String containerId;
        private String instanceType;
        
        JMeterTestExecution(String testId, String testFolder, String testFile, String containerId, String instanceType) {
            this.testId = testId;
            this.testFolder = testFolder;
            this.inputFile = testFile;
            this.containerId = containerId;
            this.instanceType = instanceType;
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
                
                // post processing 
                postProcessingData(testId, containerId, instanceType);
                
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
