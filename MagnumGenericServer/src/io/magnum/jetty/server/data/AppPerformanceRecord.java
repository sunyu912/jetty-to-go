package io.magnum.jetty.server.data;

import io.magnum.jetty.server.data.analysis.LinearSlopePredictor;
import io.magnum.jetty.server.data.analysis.ResourceThroughputPredictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AppPerformanceRecord")
public class AppPerformanceRecord {
    
    /** DynamoDB Stored Fields */
    private String containerId;    
    private String instanceType; 
    private Set<CleanedThroughputRecord> throughputList;
    private boolean enabled = true;
    
    /** Non-stored Assistant Fields */
    private List<CleanedThroughputRecord> orderredThroughputList;
    private CleanedThroughputRecord peakRecord;
    private CleanedThroughputRecord baseRecord;    
    private Double costAtPeak;
    private Integer givenThroughput;
    private Double costAtGivenThroughput;
    private CleanedThroughputRecord givenThroughputRecord;
    private Double givenLatency;
    private boolean fullFill;
    
    /** Predictor for Resource/Throughput */
    private ResourceThroughputPredictor predictor;
    
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
    
    @DynamoDBIgnore
    public List<CleanedThroughputRecord> getOrderredThroughputDataList() {
        if (orderredThroughputList == null) {
            orderredThroughputList = convertCleanedThroughputRecordSetToOrderedList(throughputList);
        }
        return orderredThroughputList;
    }
    
    /**
     * The recorded throughput/latency/perf data points are stored in Set due to 
     * the need of fitting DynamoDB mapper. This function turns it into an orderred 
     * list.
     */
    @DynamoDBIgnore
    public static List<CleanedThroughputRecord> convertCleanedThroughputRecordSetToOrderedList(Set<CleanedThroughputRecord> set) {
        List<CleanedThroughputRecord> res = new ArrayList<CleanedThroughputRecord>();
        for(CleanedThroughputRecord ctr : set) {
            res.add(ctr);
        }
        Collections.sort(res, new CleanedThroughputRecordComparator());
        return res;
    }

    @DynamoDBIgnore
    public ResourceThroughputPredictor getPredictor() {
        if (predictor == null) {
            predictor = new LinearSlopePredictor(this);
        }
        return predictor;
    }

    public void setPredictor(ResourceThroughputPredictor predictor) {
        this.predictor = predictor;
        this.predictor.setRecord(this);
    }
    
    @DynamoDBIgnore
    public CleanedThroughputRecord getMaxThroughputRecord(Double givenLatency) {
        if (givenLatency == null) {
            givenLatency = Double.MAX_VALUE * 0.8;
        }
        
        int max = 0;
        CleanedThroughputRecord maxR = null;
        for(CleanedThroughputRecord record : getThroughputList()) {
            if (record.getThroughput() > max && record.getLatency() <= (givenLatency * 1.02)) {
                max = record.getThroughput();
                maxR = record;
            }
        }
        return maxR;
    }
    
    @DynamoDBIgnore
    public CleanedThroughputRecord getMinThroughputRecord(Double givenLatency) {
        if (givenLatency == null) {
            givenLatency = Double.MAX_VALUE * 0.8;
        }
        
        int min = Integer.MAX_VALUE;
        CleanedThroughputRecord minR = null;
        for(CleanedThroughputRecord record : getThroughputList()) {
            if (record.getThroughput() > min && record.getLatency() <= (givenLatency * 1.02)) {
                min = record.getThroughput();
                minR = record;
            }
        }
        return minR;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private static class CleanedThroughputRecordComparator implements Comparator<CleanedThroughputRecord> {
        @Override
        public int compare(CleanedThroughputRecord arg0, CleanedThroughputRecord arg1) {
            int c1 = arg0.getThroughput();
            int c2 = arg1.getThroughput();
            if (c1 > c2) {
                return 1;
            } else if (c1 == c2) {
                return 0;
            } else {
                return -1;
            }
        }        
    }
}
