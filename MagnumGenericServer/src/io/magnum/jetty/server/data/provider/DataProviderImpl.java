package io.magnum.jetty.server.data.provider;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMapper;

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
    public List<Integer> getPrimeNumbers(int limit) {
        List<Integer> allNumbers = new ArrayList<Integer>();
        for(int i = 1; i <= limit; i++) {
            if (isPrime(i)) {
                allNumbers.add(i);
            }
        }
        return allNumbers;
    }	
    
    /*
     * Prime number is not divisible by any number other than 1 and itself
     * @return true if number is prime
     */
    public static boolean isPrime(int number){
        for(int i = 2; i < number; i++) {
           if(number%i == 0){
               return false; //number is divisible so its not prime
           }
        }
        return true; //number is prime now
    }
}
