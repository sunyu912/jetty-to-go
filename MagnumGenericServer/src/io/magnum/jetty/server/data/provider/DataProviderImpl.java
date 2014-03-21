package io.magnum.jetty.server.data.provider;

import java.util.List;

import io.magnum.jetty.server.data.ScreenshotRecord;

import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBScanExpression;

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
	@SuppressWarnings("unused")
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
}
