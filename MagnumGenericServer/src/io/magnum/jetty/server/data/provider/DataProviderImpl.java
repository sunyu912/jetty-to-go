package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.BatchRunHistoryRecord;
import io.magnum.jetty.server.data.BatchRunRecord;
import io.magnum.jetty.server.data.BatchRunResultRecord;
import io.magnum.jetty.server.data.ScreenshotRecord;

import java.util.Arrays;
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
    public void addScreenshotRecord(ScreenshotRecord record) {
        dynamoDBMapper.save(record);
    }

    @Override
    public List<ScreenshotRecord> listScreenshots(String url) {
        if (url == null) {
            DynamoDBScanExpression expression = new DynamoDBScanExpression();
            return dynamoDBMapper.scan(ScreenshotRecord.class, expression);
        }
        return null;
    }

    @Override
    public void addGenericObject(Object obj) {
        dynamoDBMapper.save(obj);        
    }

    @Override
    public List<BatchRunRecord> listBatchRuns(String id) {
        if (id == null) {
            DynamoDBScanExpression expression = new DynamoDBScanExpression();
            return dynamoDBMapper.scan(BatchRunRecord.class, expression);
        } else {
            BatchRunRecord r = dynamoDBMapper.load(BatchRunRecord.class, id);
            return Arrays.asList(new BatchRunRecord[]{r});
        }
    }

    @Override
    public List<BatchRunHistoryRecord> listBatchHistory(String id) {
        AttributeValue attributeValue = new AttributeValue().withS(id);
        DynamoDBQueryExpression expression = new DynamoDBQueryExpression(attributeValue); 
        return dynamoDBMapper.query(BatchRunHistoryRecord.class, expression);
    }

    @Override
    public List<BatchRunResultRecord> listBatchRunResult(Long timestamp) {
        AttributeValue attributeValue = new AttributeValue().withN(timestamp.toString());
        DynamoDBQueryExpression expression = new DynamoDBQueryExpression(attributeValue); 
        return dynamoDBMapper.query(BatchRunResultRecord.class, expression);
    }	
}
