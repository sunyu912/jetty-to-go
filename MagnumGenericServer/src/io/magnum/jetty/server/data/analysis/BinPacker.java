package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.CleanedThroughputRecord;
import io.magnum.jetty.server.data.ColocationTestRecord;
import io.magnum.jetty.server.data.CotestAppInfo;
import io.magnum.jetty.server.data.InstanceResource;
import io.magnum.jetty.server.data.Pricing;
import io.magnum.jetty.server.data.ResourceAllocation;
import io.magnum.jetty.server.data.provider.DataProvider;
import io.magnum.jetty.server.data.shared.ThroughputRecord;
import io.magnum.jetty.server.util.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hd4ar.awscommon.exec.Exec;

public abstract class BinPacker {
    
    protected static final Logger logger = LoggerFactory.getLogger(BinPacker.class);        
    
    private static final Cache<String, List<AppPerformanceRecord>> recordCache = 
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();
    
    private DataProvider dataProvider;
    private boolean enableCotest;
    
    public BinPacker(DataProvider provider, boolean enableCotest) {
        this.dataProvider = provider;
        this.enableCotest = enableCotest;
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
            logger.debug("Missed cache!");
        } else {
            logger.debug("Hit cache!");
        }
        return records;
    }
    
    public void colocationVerification(InstanceResource ir, ApplicationAllocation aa) {
        if (aa == null) {
            return;
        }        
        // create config file
        ColocationTestRecord cotestRecord = new ColocationTestRecord();
        cotestRecord.setTimestamp(System.currentTimeMillis());
        cotestRecord.setType(ir.getInstanceType());
        List<CotestAppInfo> apps = new ArrayList<CotestAppInfo>();
        for(ApplicationAllocation a : ir.getAllocatedApplications()) {
            CotestAppInfo app = new CotestAppInfo();
            app.setContainerId(a.getContainerId());
            app.setCpu((int) (a.getCpu()));
            app.setTarget(false);
            app.setStep(1);
            app.setThroughput(a.getAllocatedThroughput());
            apps.add(app);
        }
        
        // put target app
        CotestAppInfo app = new CotestAppInfo();
        app.setContainerId(aa.getContainerId());
        app.setCpu((int) (aa.getCpu()));
        app.setTarget(true);
        app.setStep(5);
        app.setThroughput(aa.getAllocatedThroughput());
        apps.add(app);
        cotestRecord.setApps(apps);
        
        try {
            JsonMapper.mapper.writeValue(new File("/tmp/cotest.json"), cotestRecord);
        } catch (IOException e) {
            logger.error("Failed to generate the cotest json config", e);
        }
        
        // run
        Exec exec = new Exec("/usr/local/bin/fab --fabfile=/Users/yusun/workspace/roar-controller/roar-fabfile4.py performance_measurement", 0, 1000 * 60 * 10);
        try {
            logger.info("Executing cotest...");
            exec.execute();
            logger.info("Finished cotest.");
        } catch (IOException e) {
            logger.error("Failed to execute the test plan", e);
        }
        
        // check result
        try {
            ColocationTestRecord cotest = JsonMapper.mapper.readValue(new File("/tmp/cotest.json"), ColocationTestRecord.class);
            
            // analysis
            int maxSupportedStepIndex = Integer.MAX_VALUE;
            for(ApplicationAllocation existingApp : ir.getAllocatedApplications()) {
                logger.info("Verifying allocated app {} with target throughtput {} and latency {}", 
                        existingApp.getContainerId(), existingApp.getAllocatedThroughput(), existingApp.getMinLatency());
                CotestAppInfo appInfo = cotest.getAppByName(existingApp.getContainerId());
                int i = -1;
                for(Entry<Long, ThroughputRecord> entry : appInfo.getResult().getCapturedThroughputPoints().entrySet()) {
                    int throughput = entry.getValue().getCount();
                    int latency = entry.getValue().getMean();
                    if (throughput < (existingApp.getAllocatedThroughput() * 0.95)
                            || latency > (existingApp.getMinLatency() * 1.1)) {
                        break;
                    }
                    i++;
                }
                logger.info("The maximum supported index is {}", i);
                if (i < maxSupportedStepIndex) {
                    maxSupportedStepIndex = i;
                }
            }
            logger.info("The maximum supported index based on existing apps is {}", maxSupportedStepIndex);
            CotestAppInfo appInfo = cotest.getTargetApp();
            int i = -1;
            int finalThroughput = 0;
            for(Entry<Long, ThroughputRecord> entry : appInfo.getResult().getCapturedThroughputPoints().entrySet()) {
                int throughput = entry.getValue().getCount();
                int latency = entry.getValue().getMean();
                if (latency > (aa.getMinLatency() * 1.1) || i > maxSupportedStepIndex ) {                                        
                    break;
                }
                if (throughput > finalThroughput) {
                    finalThroughput = throughput;
                }
                i++;
                logger.info("Target app passes at index {} with throughput {}", i, finalThroughput);
            }
            
            if (i == -1) {
                logger.info("Unfortunately, the allocated resource cannot meet the QoS requirement");
                aa = null;
            } else {                
                if (finalThroughput < aa.getAllocatedThroughput() * 0.9) {
                    logger.info("We adjusted the throughput for the target app from {} to {}", 
                            aa.getAllocatedThroughput(), finalThroughput);
                    aa.setAllocatedThroughput(finalThroughput);
                } else {
                    logger.info("We do NOT adjust the throughput for the target app from {} to {}", 
                            aa.getAllocatedThroughput(), finalThroughput);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get the result cotest", e);
        }                
    }
    
    
    
    private void fillCandidateToBins(ApplicationCandidate firstCandidate,
            List<InstanceResource> allocatedResources) {
        
        // locate the first available bin
        logger.info("Iteration {}: Total allocated bins: {}", index, allocatedResources.size());
        InstanceResource chosenBin = null;
        boolean allocatedOnce = false;
        for(InstanceResource ir : allocatedResources) {
            logger.info("Iteration: {} Bin Id: {} Type: {} CPU Remaining: {} Mem Remainging: {} Available for more: {}", index,
                    ir.getId(), ir.getInstanceType(), ir.getRemainingCpu(), ir.getRemainingMem(), ir.hasRemaining());
            if (ir.hasRemaining()) {
                // try to fill the remaining portion
                chosenBin = ir;                
                ApplicationAllocation aa = collocateApplication(firstCandidate, chosenBin);
                if (enableCotest) {
                    colocationVerification(ir, aa);
                }
                if (aa != null) {                    
                    chosenBin.getAllocatedApplications().add(aa);
                    logger.info("Iteration {}: colocate {} in the existing bin type {} with throughput {}", index, 
                            firstCandidate.getContainerId(), chosenBin.getInstanceType(), aa.getAllocatedThroughput());
                    allocatedOnce = true;
                    firstCandidate.setRemainingThroughput(firstCandidate.getRemainingThroughput() - aa.getAllocatedThroughput());
                    break;
                }
            }
        }
        
        // if no existing bins is available, put a new bin for the application
        if (!allocatedOnce) {
            chosenBin = new InstanceResource();
            int binIndex = allocatedResources.size() + 1;
            chosenBin.setId(Integer.toString(binIndex));
            ApplicationAllocation aa = allocationApplication(firstCandidate);
            if (aa != null) {
                chosenBin.getAllocatedApplications().add(aa);
                chosenBin.setInstanceType(firstCandidate.getCurrentFirstChoice().getInstanceType());
                allocatedResources.add(chosenBin);
                logger.info("Iteration {}: allocate a new bin type {} for {} with throughput {}", index, 
                        firstCandidate.getCurrentFirstChoice().getInstanceType(), 
                        firstCandidate.getContainerId(), aa.getAllocatedThroughput());
            } else {
                logger.warn("Iteration {}: Failed to allocate bin for application {}",
                        index, firstCandidate.getContainerId());
                throw new RuntimeException("Terminating the bin-packing because of unavailability for application " 
                        + firstCandidate.getContainerId());
            }
        }                
    }
    
    private ApplicationAllocation collocateApplication(ApplicationCandidate firstCandidate,
            InstanceResource chosenBin) {
        double cpu = chosenBin.getRemainingCpu();
        double mem = chosenBin.getRemainingMem();
        String instanceType = chosenBin.getInstanceType();
        AppPerformanceRecord record = getAnalysisRecordsData(firstCandidate.getContainerId(), instanceType);
        if (record == null) {
            return null;
        }
        ApplicationAllocation aa = record.getPredictor().predictThroughput(
                cpu, mem, null, null, firstCandidate.getRemainingThroughput(), firstCandidate.getTargetLatency());
        if (aa != null) {
            aa.setContainerId(firstCandidate.getContainerId());
            aa.setMinLatency(firstCandidate.getTargetLatency());
//            firstCandidate.setRemainingThroughput(firstCandidate.getRemainingThroughput() - aa.getAllocatedThroughput());
        }
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
            aa.setMinLatency(candidate.getTargetLatency());
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
                aa.setMinLatency(candidate.getTargetLatency());
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
            logger.info("\n\nIteration {}: ", index);
            
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
    
    public void processColocationResult() {
        
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
