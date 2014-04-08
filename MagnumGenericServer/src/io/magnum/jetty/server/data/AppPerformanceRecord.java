package io.magnum.jetty.server.data;

import java.util.Set;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AppPerformanceRecord")
public class AppPerformanceRecord {
    
    private String containerId;    
    private String instanceType; 
    private Set<CleanedThroughputRecord> throughputList;
    
    private CleanedThroughputRecord peakRecord;
    private CleanedThroughputRecord baseRecord;    
    private Double costAtPeak;
    private Integer givenThroughput;
    private Double costAtGivenThroughput;
    private CleanedThroughputRecord givenThroughputRecord;
    private Double givenLatency;
    private boolean fullFill;
    
    @DynamoDBHashKey(attributeName = "containerId")
    public String getContainerId() {
        return containerId;
    }
    
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
    
    @DynamoDBRangeKey(attributeName = "instanceType")
    public String getInstanceType() {
        return instanceType;
    }
    
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }
    
    @DynamoDBMarshalling (marshallerClass = CleanedThroughputRecordListMarshaller.class)
    public Set<CleanedThroughputRecord> getThroughputList() {
        return throughputList;
    }
    
    public void setThroughputList(Set<CleanedThroughputRecord> throughputList) {
        this.throughputList = throughputList;
    }

    @DynamoDBIgnore
    public CleanedThroughputRecord getPeakRecord() {
        return peakRecord;
    }

    public void setPeakRecord(CleanedThroughputRecord peakRecord) {
        this.peakRecord = peakRecord;
    }

    @DynamoDBIgnore
    public CleanedThroughputRecord getBaseRecord() {
        return baseRecord;
    }

    public void setBaseRecord(CleanedThroughputRecord baseRecord) {
        this.baseRecord = baseRecord;
    }

    @DynamoDBIgnore
    public Double getCostAtPeak() {
        if (costAtPeak != null) {
            costAtPeak = Math.round(costAtPeak * 100000.0) / 100000.0;
        }
        return costAtPeak;
    }

    public void setCostAtPeak(Double costAtPeak) {
        this.costAtPeak = costAtPeak;
    }

    @DynamoDBIgnore
    public Integer getGivenThroughput() {
        return givenThroughput;
    }

    public void setGivenThroughput(Integer givenThroughput) {
        this.givenThroughput = givenThroughput;
    }

    @DynamoDBIgnore
    public Double getGivenLatency() {
        return givenLatency;
    }

    public void setGivenLatency(Double givenLatency) {
        this.givenLatency = givenLatency;
    }

    @DynamoDBIgnore
    public Double getCostAtGivenThroughput() {
        if (costAtGivenThroughput != null) {
            costAtGivenThroughput = Math.round(costAtGivenThroughput * 100000.0) / 100000.0;
        }
        return costAtGivenThroughput;
    }

    public void setCostAtGivenThroughput(Double costAtGivenThroughput) {
        this.costAtGivenThroughput = costAtGivenThroughput;
    }

    @DynamoDBIgnore
    public CleanedThroughputRecord getGivenThroughputRecord() {
        return givenThroughputRecord;
    }

    public void setGivenThroughputRecord(CleanedThroughputRecord givenThroughputRecord) {
        this.givenThroughputRecord = givenThroughputRecord;
    }

    @DynamoDBIgnore
    public boolean isFullFill() {
        return fullFill;
    }

    public void setFullFill(boolean fullFill) {
        this.fullFill = fullFill;
    }
}
