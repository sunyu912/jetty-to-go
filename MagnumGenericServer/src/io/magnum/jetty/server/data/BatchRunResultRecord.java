package io.magnum.jetty.server.data;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "BatchRunResultRecord")
public class BatchRunResultRecord {

    private Long timestamp;
    private Long sequence;
    private ScreenshotRecord resultRecord;

    @DynamoDBHashKey(attributeName = "timestamp")
    public Long getTimestamp() {
        return timestamp;
    }
        
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @DynamoDBRangeKey(attributeName = "sequence")
    public Long getSequence() {
        return sequence;
    }
    
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    @DynamoDBMarshalling (marshallerClass = ScreenshotRecordMarshaller.class)
    public ScreenshotRecord getResultRecord() {
        return resultRecord;
    }

    public void setResultRecord(ScreenshotRecord resultRecord) {
        this.resultRecord = resultRecord;
    }
}
