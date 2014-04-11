package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.CleanedThroughputRecord;
import io.magnum.jetty.server.data.InstanceResource;
import io.magnum.jetty.server.data.Pricing;
import io.magnum.jetty.server.data.ResourceAllocation;
import io.magnum.jetty.server.data.provider.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public abstract class BinPacker {
    
    protected static final Logger logger = LoggerFactory.getLogger(BinPacker.class);        
    
    private static final Cache<String, List<AppPerformanceRecord>> recordCache = 
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();
    
    private DataProvider dataProvider;
    
    public BinPacker(DataProvider provider) {
        this.dataProvider = provider;
    }
    
    protected int index = 0;
    
    abstract void sortApplicationCandidates(List<ApplicationCandidate> candidates);
    
    abstract void sortBins(List<InstanceResource> bins);
    
    private AppPerformanceRecord getAnalysisRecordsData(String containerId, String instanceType) {
        List<AppPerformanceRecord> records = getAppPerformanceRecords(containerId);
        for(AppPerformanceRecord r : records) {
            if (r.getInstanceType().endsWith(instanceType)) {                
                return r;
            }
        }
        return null;
    } 
    
    private List<AppPerformanceRecord> getAppPerformanceRecords(String containerId) {
        List<AppPerformanceRecord> records = recordCache.getIfPresent(containerId);
        if (records == null) {
            records = dataProvider.listAppPerformanceRecord(containerId);
            recordCache.put(containerId, records);
            logger.info("Missed cache!");
        } else {
            logger.info("Hit cache!");
        }
        return records;
    }
    
    private void fillCandidateToBins(ApplicationCandidate firstCandidate,
            List<InstanceResource> allocatedResources) {
        
        // locate the first available bin
        InstanceResource chosenBin = null;
        for(InstanceResource ir : allocatedResources) {
            if (ir.hasRemaining()) {
                chosenBin = ir;
                break;
            }        
        }
        // if no existing bins is available, put a new bin for the application
        if (chosenBin == null) {            
            chosenBin = new InstanceResource();
            int binIndex = allocatedResources.size() + 1;
            chosenBin.setId(Integer.toString(binIndex));
            ApplicationAllocation aa = allocationApplication(firstCandidate);
            chosenBin.getAllocatedApplications().add(aa);
            chosenBin.setInstanceType(firstCandidate.getCurrentFirstChoice().getInstanceType());
            allocatedResources.add(chosenBin);
            logger.info("Interation {}: allocate a new bin type {} for {}", index, 
                    firstCandidate.getCurrentFirstChoice().getInstanceType(), firstCandidate.getContainerId());
        } else {
            chosenBin.getAllocatedApplications().add((collocateApplication(firstCandidate, chosenBin)));
            logger.info("Interation {}: colocate {} in the existing bin type {}", index, 
                    firstCandidate.getContainerId(), chosenBin.getInstanceType());
        }
    }
    
    private ApplicationAllocation collocateApplication(ApplicationCandidate firstCandidate,
            InstanceResource chosenBin) {
        double cpu = chosenBin.getRemainingCpu();
        double mem = chosenBin.getRemainingMem();
        String instanceType = chosenBin.getInstanceType();
        AppPerformanceRecord record = getAnalysisRecordsData(firstCandidate.getContainerId(), instanceType);
        ApplicationAllocation aa = record.getPredictor().predictThroughput(
                cpu, mem, null, null, firstCandidate.getTargetLatency());
        logger.info("TEST: aa" + aa);
        firstCandidate.setRemainingThroughput(firstCandidate.getRemainingThroughput() - aa.getAllocatedThroughput());
        return aa;
    }

    private ApplicationAllocation allocationApplication(ApplicationCandidate candidate) {
        ApplicationAllocation aa = new ApplicationAllocation();
        aa.setContainerId(candidate.getContainerId());
        if (candidate.getCurrentFirstChoice().isFullFill()) {
            aa.setAllocatedThroughput(candidate.getCurrentFirstChoice().getPeakRecord().getThroughput());
            aa.setCpu(candidate.getCurrentFirstChoice().getPeakRecord().getCpu());
            aa.setMem(candidate.getCurrentFirstChoice().getPeakRecord().getMem());
            aa.setNetwork(candidate.getCurrentFirstChoice().getPeakRecord().getNetwork());
            aa.setDisk(candidate.getCurrentFirstChoice().getPeakRecord().getDisk());
            candidate.setRemainingThroughput(candidate.getRemainingThroughput() 
                    - candidate.getCurrentFirstChoice().getPeakRecord().getThroughput());
        } else {            
            CleanedThroughputRecord givenTrhRecord = candidate.getCurrentFirstChoice().getGivenThroughputRecord();
            if (givenTrhRecord != null) {
                aa.setAllocatedThroughput(candidate.getRemainingThroughput());
                aa.setCpu(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getCpu());
                aa.setMem(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getMem());
                aa.setNetwork(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getNetwork());
                aa.setDisk(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getDisk());
                candidate.setRemainingThroughput(0);
            } else {
                logger.info("The throughput {} cannot fit in this instance", candidate.getRemainingThroughput());
            }
        }
        return aa; 
    }
    
    protected List<AppPerformanceRecord> orderAppRecordsBasedOnCost(String containerId, Integer throughput, Double latency) {
        List<AppPerformanceRecord> records = getAppPerformanceRecords(containerId);            
        
        for(AppPerformanceRecord r : records) {
            CleanedThroughputRecord peakRecord = r.getMaxThroughputRecord(latency);
            r.setPeakRecord(peakRecord);
            if (peakRecord != null) {
                r.setCostAtPeak(Pricing.getCostPerThroughput(r.getInstanceType(), peakRecord.getThroughput()));
                r.setGivenLatency(latency);
                r.setGivenThroughput(throughput);
                if (throughput != null) {
                    if (throughput >= r.getPeakRecord().getThroughput()) {
                        r.setCostAtGivenThroughput(r.getCostAtPeak());
                        r.setGivenThroughputRecord(peakRecord);
                        r.setFullFill(true);
                    } else {
                        r.setCostAtGivenThroughput(Pricing.getCostPerThroughput(r.getInstanceType(), throughput));
                        r.setGivenThroughputRecord(r.getPredictor().predictResourceInCTR(throughput, latency));
                        r.setFullFill(false);
                    }
                } else {
                    r.setCostAtGivenThroughput(r.getCostAtPeak());
                    r.setGivenThroughputRecord(peakRecord);
                    r.setFullFill(true);
                }
            }
        }
        
        List<AppPerformanceRecord> orderedList = new ArrayList<AppPerformanceRecord>();        
        for(AppPerformanceRecord record : records) {
            orderedList.add(record);
        }
        
        Collections.sort(orderedList, new AppPerformanceRecordComparator());
        return orderedList;
    }        
    
    public ResourceAllocation binPacking(List<ApplicationCandidate> candidates) {
        logger.info("****** BIN PACKING ******");
        
        // the final bin/item configuration
        ResourceAllocation resourceAllocation = new ResourceAllocation();
                
        // terminate when there are not items in the list
        while (candidates.size() > 0) {
            
            index++;
                        
            // sort items
            logger.info("Iteration {}: Sorting all items", index);
            sortApplicationCandidates(candidates);
            
            // get the first item
            ApplicationCandidate firstCandidate = candidates.get(0);
            logger.info("Iteration {}: First Item {} Remaining Throughput {}", index, 
                    firstCandidate.getContainerId(), firstCandidate.getRemainingThroughput());            
            
            // sort existing bins
            logger.info("Iteration {}: Sorting all bins", index);
            sortBins(resourceAllocation.getAllocatedResources());
            
            // fill item to a bin
            fillCandidateToBins(firstCandidate, resourceAllocation.getAllocatedResources());
            
            // if the current item is done, remove it from the list
            if (firstCandidate.isDone()) {
                candidates.remove(firstCandidate);
                logger.info("Iteration {}: Candidate {} is done, remove it from the list.", 
                        index, firstCandidate.getContainerId());
            }            
        }
        
        return resourceAllocation;
    }   
    
    private class AppPerformanceRecordComparator implements Comparator<AppPerformanceRecord> {
        @Override
        public int compare(AppPerformanceRecord arg0, AppPerformanceRecord arg1) {
            Double c1 = arg0.getCostAtGivenThroughput() == null ? Double.MAX_VALUE : arg0.getCostAtGivenThroughput();
            Double c2 = arg1.getCostAtGivenThroughput() == null ? Double.MAX_VALUE : arg1.getCostAtGivenThroughput();
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
