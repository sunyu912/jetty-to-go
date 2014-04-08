package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.BenchmarkRecord;
import io.magnum.jetty.server.data.TestInfo;

import java.util.List;

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
}
