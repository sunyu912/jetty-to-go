package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.CleanedThroughputRecord;
import io.magnum.jetty.server.data.shared.GlobalDataCollectorJsonWrapper;
import io.magnum.jetty.server.data.shared.HostPerfRecordTimeMap;
import io.magnum.jetty.server.data.shared.PerfRecord;
import io.magnum.jetty.server.data.shared.ThroughputRecord;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

public class TestDataPostProcesser {

    private GlobalDataCollectorJsonWrapper data;
    private int peakThroughput;
    private Set<CleanedThroughputRecord> cleanedList;
    
    public TestDataPostProcesser(GlobalDataCollectorJsonWrapper data) {
        this.setData(data);     
        this.cleanedList = new LinkedHashSet<CleanedThroughputRecord>();
        captureValidPoints();
    }
    
    public Set<CleanedThroughputRecord> getCleanedList() {
        return cleanedList;
    }
        
    public void captureValidPoints() {
        int steps = getData().getSteps();
        int warmup = getData().getWarmupDuration() + 5;
        int duration = getData().getDuration();
        
        if (steps == 0) {
            steps = 5;
        }
        
        int start = warmup;
        int stepRate = (duration - 5 * steps) / steps;
        
        Collection<ThroughputRecord> records = getData().getThroughputMap().values();
        
        for(int i = 0; i < steps; i++) {
            CleanedThroughputRecord cleanedRecord = new CleanedThroughputRecord();
            
            int end = start + 5 + stepRate;
            int actualStart = start + 5 + stepRate / 2;
            
            // throughput / latency
            int sum = 0;
            int count = 0;
            int latencySum = 0;
            int errorSum = 0;
            Long startTime = null;
            
            for(ThroughputRecord r : records) {
                int index = Integer.parseInt(r.getLabel()); 
                if (index >= actualStart && index < end) {
                    sum += (r.getCount() / r.getPeriodInSec());
                    latencySum += r.getMean();
                    errorSum += r.getErrorCount();
                    if (startTime == null) {
                        startTime = r.getStartTime();
                    }
                    count ++;
                }
                if (index >= end) {
                    break;
                }
            }
            
            if (count > 0) {
                ThroughputRecord avgPoint = new ThroughputRecord();
                avgPoint.setCount(sum / count);
                avgPoint.setLabel(Integer.toString(actualStart));
                avgPoint.setMean(latencySum / count);
                avgPoint.setErrorCount(errorSum / count);
                
                getData().getCapturedThroughputPoints().put(startTime, avgPoint);
                // cleaned record
                cleanedRecord.setThroughput(avgPoint.getCount());
                cleanedRecord.setLatency(avgPoint.getMean());
                
                if (avgPoint.getCount() > peakThroughput) {
                    peakThroughput = avgPoint.getCount();
                }
            }
            
            // perf data
            for(Entry<String, HostPerfRecordTimeMap> entry : getData().getPerfMap().entrySet()) {                
                Long startTimestamp = null;
                Long recordTimestamp = null;
                double cpuSum = 0;
                double memSum = 0;
                double netSum = 0;
                double diskSum = 0;
                count = 0;
                for(Entry<Long, PerfRecord> ee : entry.getValue().entrySet()) {
                    PerfRecord r = ee.getValue();
                    if (startTimestamp == null) {
                        startTimestamp = ee.getKey();
                    }
                    
                    int index = (int) ((ee.getKey() - startTimestamp) / 1000);
                    if (index >= actualStart && index <= end) {
                        if (recordTimestamp == null) {
                            recordTimestamp = ee.getKey();
                        }
                        cpuSum += (100 - r.getCpuIdle());
                        memSum += r.getMemory();
                        netSum += r.getNetworkEth0();
                        diskSum += r.getDisk();
                        count++;
                    }
                    
                    if (index > end) {
                        break;
                    }
                }
                
                if (count > 0) {
                    PerfRecord rr = new PerfRecord();
                    rr.setCpu(cpuSum / count);
                    rr.setMemory(memSum / count);
                    rr.setNetworkEth0(netSum / count);
                    rr.setDisk(diskSum / count);
                    HostPerfRecordTimeMap perfMap = getData().getCapturedPerfPoints().get(entry.getKey());
                    if (perfMap == null) {
                        perfMap = new HostPerfRecordTimeMap();
                    }
                    perfMap.put(recordTimestamp, rr);
                    getData().getCapturedPerfPoints().put(entry.getKey(), perfMap);
                    
                    // cleaned record
                    cleanedRecord.setCpu(rr.getCpu());
                    cleanedRecord.setMem(rr.getMemory());
                    cleanedRecord.setNetwork(rr.getNetworkEth0());
                    cleanedRecord.setDisk(rr.getDisk());
                    
                    this.cleanedList.add(cleanedRecord);
                }
            }
            
            start = end;
            this.data.setPeakThroughput(peakThroughput);
        }
    }

    public GlobalDataCollectorJsonWrapper getData() {
        return data;
    }

    public void setData(GlobalDataCollectorJsonWrapper data) {
        this.data = data;
    }

    public int getPeakThroughput() {
        return peakThroughput;
    }

    public void setPeakThroughput(int peakThroughput) {
        this.peakThroughput = peakThroughput;
    }
}
