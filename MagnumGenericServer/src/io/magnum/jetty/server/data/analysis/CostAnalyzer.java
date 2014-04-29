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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CostAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(CostAnalyzer.class);
    
    private static final Cache<String, List<AppPerformanceRecord>> recordCache = 
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();
    
    @Autowired
    private DataProvider dataProvider;
    
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
    
    public List<AppPerformanceRecord> listCost(String containerId, Integer throughput, Double latency) {
        List<AppPerformanceRecord> records = getAppPerformanceRecords(containerId);            
        
        for(AppPerformanceRecord r : records) {
            CleanedThroughputRecord peakRecord = getMaxThroughputRecord(r, latency);
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
                        r.setGivenThroughputRecord(estimateRecord(r, throughput));
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
    
    private CleanedThroughputRecord estimateRecord(
            AppPerformanceRecord record, Integer throughput) {
        List<CleanedThroughputRecord> orderedThroughputRecords = 
                record.getOrderredThroughputDataList();        
        
        CleanedThroughputRecord res = new CleanedThroughputRecord();
        CleanedThroughputRecord pre = null;
        for(CleanedThroughputRecord r : orderedThroughputRecords) {
            if (throughput < r.getThroughput()) {
                if (pre == null) {
                    res = r;
                } else {
                    res.setThroughput(throughput);
                    res.setLatency((r.getLatency() + pre.getLatency()) / 2);
                    res.setCpu((r.getCpu() + pre.getCpu()) / 2);
                    res.setMem((r.getMem() + pre.getMem()) / 2);
                    res.setNetwork((r.getNetwork() + pre.getNetwork()) / 2);
                    res.setDisk((r.getDisk() + pre.getDisk()) / 2);
                }
                break;
            }
            pre = r;
        }
        
        return res;
    }        

    public ResourceAllocation getFinalSolution(String containerId, Integer throughput, Double latency) {        
        ResourceAllocation resourceAllocation = new ResourceAllocation();
        if (throughput == null) {
            return resourceAllocation;
        }
        Integer remainingThroughput = throughput;
        int index = 0;
        while (remainingThroughput > 0) {
            List<AppPerformanceRecord> orderedList = listCost(containerId, remainingThroughput, latency);
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
                CleanedThroughputRecord tmp = estimateRecord(bestChoice, remainingThroughput);
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
    
    public ResourceAllocation binPacking(List<ApplicationCandidate> candidates, boolean enableCotest) {
        BinPacker binPacker = new BinPackerImpl1(dataProvider, enableCotest);
        return binPacker.binPacking(candidates);
    }
    
    public ResourceAllocation binPacking2(List<ApplicationCandidate> candidates, boolean enableCotest) {
        BinPacker binPacker = new BinPackerImpl2(dataProvider, enableCotest);
        return binPacker.binPacking(candidates);
    }
    
    public ResourceAllocation binPacking(BinPacker binPacker, List<ApplicationCandidate> candidates, boolean enableCotest) {
        return binPacker.binPacking(candidates);
    }
    
    public ResourceAllocation applicationsBinPacking(List<ApplicationCandidate> candidates) {
        logger.info("****** BIN PACKING ******");
        ResourceAllocation resourceAllocation = new ResourceAllocation();
                
        int index = 0;
        while (candidates.size() > 0) {
            logger.info("Sorting all cost");
            index++;
                        
            // fit the best choice for each
            for(ApplicationCandidate candidate : candidates) {
                List<AppPerformanceRecord> orderredChoices = listCost(candidate.getContainerId(), candidate.getRemainingThroughput(), candidate.getTargetLatency());
                candidate.setCurrentFirstChoice(orderredChoices.get(0));
            }
            
            // choose the biggest one to allocate
            Collections.sort(candidates, new ApplicationCandidateComparator());
            
            ApplicationCandidate firstCandidate = candidates.get(0);
            logger.info("Step " + index + ": " + firstCandidate.getContainerId() 
                    + " - " + firstCandidate.getCurrentFirstChoice().isFullFill());
            
            // OK, allocate one instance based on the biggest/most cost-effective instance chosen
            InstanceResource ir = new InstanceResource();
            ir.setId(Integer.toString(index));
            // fill this instance resource before going to the next
            ApplicationAllocation aa = allocationApplication(firstCandidate);
            ir.getAllocatedApplications().add(aa);
            ir.setInstanceType(firstCandidate.getCurrentFirstChoice().getInstanceType());
            logger.info("Step " + index + ": allocate " + ir.getInstanceType() + " cost: " + ir.getCost());
            // fill the remaining 
            while (ir.hasRemaining()) {
                ApplicationAllocation a = findRightApplicationForRemaining(ir, candidates);
                if (a != null && a.isAllocated()) {
                    ir.getAllocatedApplications().add(a);
                } else {
                    break;
                }
            }
            
            resourceAllocation.getAllocatedResources().add(ir);
            
            // if done, remove
            if (firstCandidate.isDone()) {
                candidates.remove(firstCandidate);
                logger.info("Candidate {} is done, remove it from the list.", firstCandidate.getContainerId());
            }            
        }
        
        return resourceAllocation;
    }
    
    private ApplicationAllocation findRightApplicationForRemaining(
            InstanceResource ir, List<ApplicationCandidate> candidates) {        
        
        // 1. calculate the current total cost
        logger.info("Step 1. Calculate the total cost");
        Map<String, Double> costMap = new HashMap<String, Double>();        
        for(ApplicationCandidate ac : candidates) {
            if (ac.isDone()) continue;
            ResourceAllocation ra = 
                    getFinalSolution(ac.getContainerId(), ac.getRemainingThroughput(), ac.getTargetLatency());
            costMap.put(ac.getContainerId(), ra.getTotalCost());
            logger.info("Current original cost for {} is {} ", ac.getContainerId(), ra.getTotalCost());
        }
        
        // 2. calculate the current remaining fit
        logger.info("Step 2. Calculate the remaining fit");
        Map<String, ApplicationAllocation> remainingFit = new HashMap<String, ApplicationAllocation>();
        for(ApplicationCandidate ac : candidates) {
            if (ac.isDone()) continue;
            ApplicationAllocation aa = predictThroughputBasedOnResource(ir, ac.getContainerId());
            remainingFit.put(ac.getContainerId(), aa);
            logger.info("Remaining fit for {} is {}", ac.getContainerId(), aa.getAllocatedThroughput());
        }
        
        // 3. calculate the new total cost after the current remaining fit
        // 4. calculate the savings (1. - 3.)
        logger.info("Step 3. Calculate the savings");
        for(ApplicationCandidate ac : candidates) {
            if (ac.isDone()) continue;
            int savedThroughput = remainingFit.get(ac.getContainerId()).getAllocatedThroughput();
            ResourceAllocation ra = 
                    getFinalSolution(ac.getContainerId(), 
                            ac.getRemainingThroughput() - savedThroughput, ac.getTargetLatency());
            
            Double originalCost = costMap.get(ac.getContainerId());
            Double savedCost = originalCost - ra.getTotalCost();
            costMap.put(ac.getContainerId(), savedCost);
        }
        
        // 5. sort the savings
        costMap = sortByComparator(costMap);
        String chosenContainerId = null;
        for (Map.Entry<String, Double> entry : costMap.entrySet()) {
            logger.info("Savings for {} is {}", entry.getKey(), entry.getValue());
            chosenContainerId = entry.getKey();
        }
        
        // 6. return the biggest savings
        if (chosenContainerId == null) {
            return null;
        } else {
            return remainingFit.get(chosenContainerId);
        }
    }
    
    private ApplicationAllocation predictThroughputBasedOnResource(InstanceResource ir, String containerId) {
        ApplicationAllocation res = new ApplicationAllocation();
        List<CleanedThroughputRecord> dataRecords = getAnalysisRecordsData(containerId, ir.getInstanceType());
        // check if data is available
        res.setContainerId(containerId);
        res.setAllocatedThroughput(0);
        if (dataRecords != null) {            
            // TODO: Use linear regression WEKA
            // Right now, no predict, just find the minimum point to use and fit
            double remainingCpu = ir.getRemainingCpu();
            double remainingMem = ir.getRemainingMem();        
            for(CleanedThroughputRecord r : dataRecords) {
                if (remainingCpu >= r.getCpu() && remainingMem >= r.getMem()) {
                    if (r.getThroughput() >= res.getAllocatedThroughput()) {
                        res.setCpu(r.getCpu());
                        res.setMem(r.getMem());
                        res.setNetwork(r.getNetwork());
                        res.setDisk(r.getDisk());
                        res.setAllocatedThroughput(r.getThroughput());
                    }
                } else {
                    break;
                }
            }
        }
        return res;
    }        
    
    private List<CleanedThroughputRecord> getAnalysisRecordsData(String containerId, String instanceType) {
        List<AppPerformanceRecord> records = getAppPerformanceRecords(containerId);
        for(AppPerformanceRecord r : records) {
            if (r.getInstanceType().endsWith(instanceType)) {                
                return r.getOrderredThroughputDataList();
            }
        }
        return null;
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
            aa.setAllocatedThroughput(candidate.getRemainingThroughput());
            aa.setCpu(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getCpu());
            aa.setMem(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getMem());
            aa.setNetwork(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getNetwork());
            aa.setDisk(candidate.getCurrentFirstChoice().getGivenThroughputRecord().getDisk());
            candidate.setRemainingThroughput(0);
        }
        return aa; 
    }
    
    private CleanedThroughputRecord getMaxThroughputRecord(AppPerformanceRecord r, Double givenLatency) {
        if (givenLatency == null) {
            givenLatency = Double.MAX_VALUE * 0.8;
        }
        
        int max = 0;
        CleanedThroughputRecord maxR = null;
        for(CleanedThroughputRecord record : r.getThroughputList()) {
            if (record.getThroughput() > max && record.getLatency() <= (givenLatency * 1.02)) {
                max = record.getThroughput();
                maxR = record;
            }
        }
        return maxR;
    }
 
    private static Map sortByComparator(Map unsortMap) {
        
        List list = new LinkedList(unsortMap.entrySet());
 
        // sort list based on comparator
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                                       .compareTo(((Map.Entry) (o2)).getValue());
            }
        });
 
        // put sorted list into map again
                //LinkedHashMap make sure order in which keys were inserted
        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
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
    
    private class AppPerformanceRecordComparatorBasedOnPeakCost implements Comparator<AppPerformanceRecord> {
        @Override
        public int compare(AppPerformanceRecord arg0, AppPerformanceRecord arg1) {
            Double c1 = arg0.getCostAtPeak() == null ? Double.MAX_VALUE : arg0.getCostAtPeak();
            Double c2 = arg1.getCostAtPeak() == null ? Double.MAX_VALUE : arg1.getCostAtPeak();
            if (c1 > c2) {
                return 1;
            } else if (c1 == c2) {
                return 0;
            } else {
                return -1;
            }
        }        
    }
    
    private class ApplicationCandidateComparator implements Comparator<ApplicationCandidate> {
        @Override
        public int compare(ApplicationCandidate o1, ApplicationCandidate o2) {
            assert(o1.getCurrentFirstChoice() != null);
            assert(o2.getCurrentFirstChoice() != null);
            double cost1 = Pricing.getCost(o1.getCurrentFirstChoice().getInstanceType());
            double cost2 = Pricing.getCost(o2.getCurrentFirstChoice().getInstanceType());            
            return (int) (cost2 - cost1);
        }        
    }

    public ResourceAllocation getFinalSolution(ApplicationCandidate a) {
        return this.getFinalSolution(a.getContainerId(), a.getTargetThroughput(), a.getTargetLatency());
    }
}