package io.magnum.jetty.server.data;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="TestInfo")
public class TestInfo {
    
    public static final String FAILED = "FAILED";
    public static final String PROCESSING = "PROCESSING";
    public static final String COMPLETED = "COMPLETED";
    public static final String S3_URL_RPEFIX = "https://s3.amazonaws.com/roar-tests/";

    private String id;
    private Long timestamp;
    private String status;
    
    @DynamoDBHashKey(attributeName="id")
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }    
    
    @DynamoDBIgnore
    public String getResultModelUrl() {
        if (status.equals(COMPLETED)) {
            return S3_URL_RPEFIX + id + "/" + "throughput-perm.json";
        }        
        return null;
    }
    
    @DynamoDBIgnore
    public String getTestRecordsUrl() {
        if (status.equals(COMPLETED)) {
            return S3_URL_RPEFIX + id + "/" + "records.csv";
        }        
        return null;
    }
    
    @DynamoDBIgnore
    public String getJMeterLogUrl() {
        if (status.equals(COMPLETED)) {
            return S3_URL_RPEFIX + id + "/" + "jmeter.log";
        }        
        return null;
    }
}
