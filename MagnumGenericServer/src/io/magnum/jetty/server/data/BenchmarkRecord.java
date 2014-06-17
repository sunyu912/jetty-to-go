package io.magnum.jetty.server.data;

import io.magnum.jetty.server.util.JsonMapper;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="BenchmarkRecord")
public class BenchmarkRecord {

    private String containerId;
    private Long timestamp;
    private String instanceType;
    private String testId;
    private String notes;
    
    @DynamoDBHashKey
    public String getContainerId() {
        return containerId;
    }
    
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
    
    @DynamoDBRangeKey
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getInstanceType() {
        return instanceType;
    }
    
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }
    
    public String getTestId() {
        return testId;
    }
    
    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getNotes() {
        if (notes == null) {
            return "";
        }
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @DynamoDBIgnore
    public boolean isMtRecord() {
        if (notes == null) {
            return false;
        }
        try {
            JsonMapper.mapper.readValue(notes, Object.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
