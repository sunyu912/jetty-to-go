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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
    
    private static final Cache<String, List<AppPerformanceRecord>> recordCache2 = 
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();     
    
    private DataProvider dataProvider;
    private boolean enableCotest;
    private boolean useEnabledRecord = true;
    private String algName;
    
    public BinPacker(DataProvider provider, boolean enableCotest) {
        this.dataProvider = provider;
        this.enableCotest = enableCotest;
    }
    
    protected int index = 0;
    protected Map<String, Set<String>> cotestedMap = new HashMap<String, Set<String>>();
    
    abstract void sortApplicationCandidates(List<ApplicationCandidate> candidates);
    
    abstract void sortBins(List<InstanceResource> bins);
    
    private boolean verifiedBefore(String containerId, String irId) {
        Set<String> irIds = cotestedMap.get(containerId);
        if (irIds != null && irIds.size() > 0) {
            return irIds.contains(irId);
        }
        return false;
    }
    
    private void putVerifiedRecordInMap(String containerId, String irId) {
        Set<String> irIds = cotestedMap.get(containerId);
        if (irIds != null) {
            irIds = new HashSet<String>();
        }
        irIds.add(irId);
    }
    
    protected AppPerformanceRecord getAnalysisRecordsData(String containerId, String instanceType) {
        List<AppPerformanceRecord> records = getAppPerformanceRecords(containerId);
        for(AppPerformanceRecord r : records) {
            if (r.getInstanceType().endsWith(instanceType)) {                
                return r;
            }
        }
        return null;
    } 
    
    abstract protected boolean isCotestAllowed();
    
    private List<AppPerformanceRecord> getAppPerformanceRecords(String containerId) {
        List<AppPerformanceRecord> records = null;
        if (isUseEnabledRecord()) {
            records = recordCache.getIfPresent(containerId);
        } else {
            records = recordCache2.getIfPresent(containerId);
        }
        if (records == null) {
            if (isUseEnabledRecord()) {
                records = dataProvider.listAppPerformanceRecordEnabled(containerId);
                recordCache.put(containerId, records);
            } else {
                records = dataProvider.listAppPerformanceRecord(containerId);
                recordCache2.put(containerId, records);
            }            
            logger.info("Missed cache!");
        } else {
            logger.info("Hit cache!");
        }
        return records;
    }
    
    public void colocationVerification(InstanceResource ir, ApplicationAllocation aa) {
        if (!isCotestAllowed()) {
            return;
        }
        
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
            File f = new File("/tmp/cotest.json");
            if (f.exists()) {
                f.delete();
            }
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
                // record the cotest id
                ir.setCotestId(cotest.getTimestamp().toString());
            }
        } catch (IOException e) {
            logger.error("Failed to get the result cotest", e);
        }                
    }
    
    abstract InstanceResource handleVerifiedFailure(ApplicationCandidate firstCandidate, ApplicationAllocation aa);
    abstract ApplicationCandidate swtichFirstCandidate(List<ApplicationCandidate> candidates, InstanceResource ir);
    
    private void fillCandidateToBins(ApplicationCandidate firstCandidate, List<ApplicationCandidate> candidates,
            List<InstanceResource> allocatedResources) {
        
        // locate the first available bin
        logger.info("Iteration {}: Total allocated bins: {}", index, allocatedResources.size());
        ApplicationCandidate originalFirstCandidate = firstCandidate;
        InstanceResource chosenBin = null;
        boolean allocatedOnce = false;
        ApplicationAllocation aa = null;
        for(InstanceResource ir : allocatedResources) {
            logger.info("Iteration: {} Bin Id: {} Type: {} CPU Remaining: {} Mem Remainging: {} Available for more: {}", index,
                    ir.getId(), ir.getInstanceType(), ir.getRemainingCpu(), ir.getRemainingMem(), ir.hasRemaining());
            if (ir.hasRemaining()) {
                // try to fill the remaining portion
                chosenBin = ir;
                
                // change first candidate?
                ApplicationCandidate newAc = swtichFirstCandidate(candidates, ir);
                if (newAc != null) {
                    logger.info("Iteration: {} switch first candidate from {} to {}", index,
                            firstCandidate.getContainerId(), newAc.getContainerId());
                    firstCandidate = newAc;
                }
                
                aa = collocateApplication(firstCandidate, chosenBin);
                if (enableCotest) {
                    colocationVerification(ir, aa);
                } else {
                    //simulateColocationVerification(ir, aa);
                }
                if (aa != null && !aa.isVerifiedFailure()) {                    
                    chosenBin.getAllocatedApplications().add(aa);
                    logger.info("Iteration {}: colocate {} in the existing bin type {} with throughput {}", index, 
                            firstCandidate.getContainerId(), chosenBin.getInstanceType(), aa.getAllocatedThroughput());
                    allocatedOnce = true;
                    firstCandidate.setRemainingThroughput(firstCandidate.getRemainingThroughput() - aa.getAllocatedThroughput());
                    aa.setIndex(index);
                    
                    if (firstCandidate != originalFirstCandidate) {
                        if (firstCandidate != null && firstCandidate.isDone()) {
                            logger.info("Since the switched element has been done, remove it from the list {}", firstCandidate.getContainerId());
                            candidates.remove(firstCandidate);
                        }
                    }
                    break;
                }
            }
        }
        
        // do we only want to handle the portion of the unallocated app
        if (aa != null && aa.isVerifiedFailure()) {
            logger.info("Iteration {}: handle the failed verifiticaion situation for {}", index, firstCandidate.getContainerId());
            InstanceResource newIr = handleVerifiedFailure(firstCandidate, aa);
            if (newIr != null) {
                logger.info("Iteration {}: allocated a NEW bin but only partially filled with the failed portion {} with remaining CPU {} MEM {}", 
                        index, aa.getAllocatedThroughput(), newIr.getRemainingCpu(), newIr.getRemainingMem());
                newIr.getAllocatedApplications().get(0).setIndex(index);
                newIr.setId(Integer.toString(index));
                allocatedResources.add(newIr);
                allocatedOnce = true;
            }
        }
        
        // if no existing bins is available, put a new bin for the application
        if (!allocatedOnce) {
            firstCandidate = originalFirstCandidate;
            chosenBin = new InstanceResource();
            int binIndex = allocatedResources.size() + 1;
            chosenBin.setId(Integer.toString(binIndex));
            aa = allocationApplication(firstCandidate);
            if (aa != null) {
                aa.setIndex(index);
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
    
    private void simulateColocationVerification(InstanceResource ir, ApplicationAllocation aa) {
        if (aa == null) {
            return;
        }
        
        int cpu = (int) aa.getCpu();
        if (cpu < 8) {
            logger.info("Iteration {}: SIMULATION the allocated resource does NOT fit", index); 
            aa.setVerifiedFailure(true);
        } else {
            logger.info("Iteration {}: SIMULATION the allocated resource does fit", index);
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
        logger.info("DEBUG: " + candidate.getRemainingThroughput() + " VS " + candidate.getCurrentFirstChoice().getPeakRecord().getThroughput());
        //if (candidate.getCurrentFirstChoice().isFullFill()) {
        if (candidate.getRemainingThroughput() >= candidate.getCurrentFirstChoice().getPeakRecord().getThroughput()) {
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
            logger.info("Iteration {}: First Item {} Remaining Throughput {} + object id {}", index, 
                    firstCandidate.getContainerId(), firstCandidate.getRemainingThroughput(), firstCandidate);            
            
            // sort existing bins
            logger.info("Iteration {}: Sorting all bins", index);
            sortBins(resourceAllocation.getAllocatedResources());            
            
            // fill item to a bin
            fillCandidateToBins(firstCandidate, candidates, resourceAllocation.getAllocatedResources());
            
            // if the current item is done, remove it from the list
            if (firstCandidate.isDone()) {
                candidates.remove(firstCandidate);
                logger.info("Iteration {}: Candidate {} is done, remove it from the list.", 
                        index, firstCandidate.getContainerId());
            }
        }
        
        return resourceAllocation;
    }   
    
    public ResourceAllocation getSolutionForSingleApp(String containerId, Integer throughput, Double latency) {        
        ResourceAllocation resourceAllocation = new ResourceAllocation();
        if (throughput == null) {
            return resourceAllocation;
        }
        Integer remainingThroughput = throughput;
        int index = 0;
        while (remainingThroughput > 0) {
            List<AppPerformanceRecord> orderedList = orderAppRecordsBasedOnCost(containerId, remainingThroughput, latency);
            AppPerformanceRecord bestChoice = orderedList.get(0);
            InstanceResource instanceResource = new InstanceResource();
            index++;
            instanceResource.setId(Integer.toString(index));
            instanceResource.setInstanceType(bestChoice.getInstanceType());
            
            ApplicationAllocation applicationAllocation = new ApplicationAllocation();
            applicationAllocation.setContainerId(containerId);
            if (remainingThroughput >= bestChoice.getPeakRecord().getThroughput()) {
                applicationAllocation.setAllocatedThroughput(bestChoice.getPeakRecord().getThroughput());
                applicationAllocation.setCpu(bestChoice.getPeakRecord().getCpu());
                applicationAllocation.setMem(bestChoice.getPeakRecord().getMem());
                applicationAllocation.setNetwork(bestChoice.getPeakRecord().getNetwork());
                applicationAllocation.setDisk(bestChoice.getPeakRecord().getDisk());
                
            } else {
                applicationAllocation.setAllocatedThroughput(remainingThroughput);
                CleanedThroughputRecord tmp = bestChoice.getPredictor().predictResourceInCTR(remainingThroughput, latency);
                applicationAllocation.setCpu(tmp.getCpu());
                applicationAllocation.setMem(tmp.getMem());
                applicationAllocation.setNetwork(tmp.getNetwork());
                applicationAllocation.setDisk(tmp.getDisk());
            }                                                
            instanceResource.getAllocatedApplications().add(applicationAllocation);
            resourceAllocation.getAllocatedResources().add(instanceResource);
            remainingThroughput -= bestChoice.getPeakRecord().getThroughput();
        }
        
        return resourceAllocation;
    }
    
    public String getAlgName() {
        return algName;
    }

    public void setAlgName(String algName) {
        this.algName = algName;
    }

    public boolean isUseEnabledRecord() {
        return useEnabledRecord;
    }

    public void setUseEnabledRecord(boolean useEnabledRecord) {
        this.useEnabledRecord = useEnabledRecord;
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
