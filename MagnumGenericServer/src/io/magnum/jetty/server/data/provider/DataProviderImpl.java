package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.BenchmarkRecord;
import io.magnum.jetty.server.data.TestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodb.model.AttributeValue;

/**
 * The default implementation for DataProvider based
 * on AmazonDynamoDB.
 * <p>
 * It provides the basic data access operations for each key
 * data type used in Bolt server.
 * 
 * @author Yu Sun
 */
public class DataProviderImpl implements DataProvider {

    /**
     * DynamoDB Client
     * 
     * This client is currently only used to instantiate 
     * the object mapper. However, we keep it here just in
     * case it will be used in some other scenarios related
     * with DynamoDB. 
     */
    @SuppressWarnings("unused")
    private AmazonDynamoDB dynamoDBClient;

    /** DynamoDB Object Mapper */
    private DynamoDBMapper dynamoDBMapper;


    public DataProviderImpl(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
        this.dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
    }

    @Override
    public TestInfo updateTestInfo(String id, String status) {
        TestInfo testInfo = dynamoDBMapper.load(TestInfo.class, id);
        if (testInfo == null) {
            testInfo = new TestInfo();
            testInfo.setId(id);
            testInfo.setTimestamp(System.currentTimeMillis());
            testInfo.setStatus(status);
        } else {
            testInfo.setStatus(status);
        }        
        dynamoDBMapper.save(testInfo);
        return testInfo;        
    }

    @Override
    public TestInfo getTestInfo(String id) {        
        return dynamoDBMapper.load(TestInfo.class, id);
    }

    @Override
    public List<BenchmarkRecord> listBenchmarkRecords(String id) {
        if (id == null) {
            DynamoDBScanExpression expression = new DynamoDBScanExpression();
            return dynamoDBMapper.scan(BenchmarkRecord.class, expression);
        } else {
            AttributeValue attributeValue = new AttributeValue().withS(id);
            DynamoDBQueryExpression expression = new DynamoDBQueryExpression(attributeValue); 
            return dynamoDBMapper.query(BenchmarkRecord.class, expression);
        }        
    }

    @Override
    public void updateGeneric(Object obj) {
        dynamoDBMapper.save(obj);
    }

    @Override
    public List<AppPerformanceRecord> listAppPerformanceRecord(String containerId) {
        AttributeValue attributeValue = new AttributeValue().withS(containerId);
        DynamoDBQueryExpression expression = new DynamoDBQueryExpression(attributeValue); 
        return dynamoDBMapper.query(AppPerformanceRecord.class, expression);        
    }
    
    @Override
    public List<AppPerformanceRecord> listAppPerformanceRecordEnabled(String containerId) {
        AttributeValue attributeValue = new AttributeValue().withS(containerId);
        DynamoDBQueryExpression expression = new DynamoDBQueryExpression(attributeValue); 
        List<AppPerformanceRecord> res = dynamoDBMapper.query(AppPerformanceRecord.class, expression);
        List<AppPerformanceRecord> enabledRecords = new ArrayList<AppPerformanceRecord>();
        for(AppPerformanceRecord r : res) {
            if (r.isEnabled()) {
                enabledRecords.add(r);
            }
        }
        return enabledRecords;
    }

    @Override
    public Map<String, Integer> getAvailableApps() {
        Map<String, Integer> res = new HashMap<String, Integer>();
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<AppPerformanceRecord> records = dynamoDBMapper.scan(AppPerformanceRecord.class, scanExpression);
        for(AppPerformanceRecord r : records) {
            String name = r.getContainerId();
            Integer count = res.get(name);
            if (count == null) {
                res.put(name, 1);
            } else {
                res.put(name, count + 1);
            }
        }
        return res;
    }

    @Override
    public BenchmarkRecord getBenchmarkRecord(String id, Long timestamp) {
        return dynamoDBMapper.load(BenchmarkRecord.class, id, timestamp);
    }
}
