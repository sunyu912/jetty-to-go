package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.CleanedThroughputRecord;

abstract public class ResourceThroughputPredictor {
    
    private AppPerformanceRecord record;
    
    public ResourceThroughputPredictor() {    
    }
    
    public ResourceThroughputPredictor(AppPerformanceRecord r) {
        this.setRecord(r);
    }
    
    abstract public ApplicationAllocation predictThroughput(
            Double cpu, Double mem, Double network, Double disk, Double latency);
    
    abstract public ApplicationAllocation predictResource(
            Integer throughput, Double latency);
    
    abstract public CleanedThroughputRecord predictResourceInCTR(Integer throughput, Double latency);

    public AppPerformanceRecord getRecord() {
        return record;
    }

    public void setRecord(AppPerformanceRecord record) {
        this.record = record;
    }
}
