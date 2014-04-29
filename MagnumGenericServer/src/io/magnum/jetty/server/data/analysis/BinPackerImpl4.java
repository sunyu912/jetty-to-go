package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.InstanceResource;
import io.magnum.jetty.server.data.Pricing;
import io.magnum.jetty.server.data.ResourceAllocation;
import io.magnum.jetty.server.data.provider.DataProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BinPackerImpl4 extends BinPacker {

    public BinPackerImpl4(DataProvider provider, boolean enableCotest) {
        super(provider, enableCotest);
        setAlgName("Sort Item / Most Savings");
    }

    @Override
    protected boolean isCotestAllowed() {
        return true;
    }
    
    @Override
    void sortApplicationCandidates(List<ApplicationCandidate> candidates) {
                
        // fit the best choice for each
        for(ApplicationCandidate candidate : candidates) {
            List<AppPerformanceRecord> orderredChoices = 
                    orderAppRecordsBasedOnCost(candidate.getContainerId(), candidate.getRemainingThroughput(), candidate.getTargetLatency());
            candidate.setCurrentFirstChoice(orderredChoices.get(0));
        }
        
        // choose the biggest one to allocate
        Collections.sort(candidates, new ApplicationCandidateComparatorBasedOnInstancePrice());
    }

    @Override
    void sortBins(List<InstanceResource> bins) {
        Collections.sort(bins, new BinCandidateComparator());
    }
    
    private class BinCandidateComparator implements Comparator<InstanceResource> {
        @Override
        public int compare(InstanceResource o1, InstanceResource o2) {
//            double cost1 = o1.getRemainingCpu();
//            double cost2 = o2.getRemainingCpu();
            double cost1 = Pricing.getCost(o1.getInstanceType());
            double cost2 = Pricing.getCost(o2.getInstanceType());            
            return (int) (cost1 - cost2);
        }        
    }
    
    private class ApplicationCandidateComparatorBasedOnInstancePrice implements Comparator<ApplicationCandidate> {
        @Override
        public int compare(ApplicationCandidate o1, ApplicationCandidate o2) {
            assert(o1.getCurrentFirstChoice() != null);
            assert(o2.getCurrentFirstChoice() != null);
            double cost1 = Pricing.getCost(o1.getCurrentFirstChoice().getInstanceType());
            double cost2 = Pricing.getCost(o2.getCurrentFirstChoice().getInstanceType());            
            return (int) (cost2 - cost1);
        }        
    }

    @Override
    InstanceResource handleVerifiedFailure(ApplicationCandidate firstCandidate,
            ApplicationAllocation aa) {
        return null;
    }

    @Override
    ApplicationCandidate swtichFirstCandidate(List<ApplicationCandidate> candidates, InstanceResource ir) {
//        // 1. calculate the current total cost
//        logger.info("Step 1. Calculate the total cost");
//        Map<String, Double> costMap = new HashMap<String, Double>();        
//        for(ApplicationCandidate ac : candidates) {
//            if (ac.isDone()) continue;
//            ResourceAllocation ra = 
//                    getSolutionForSingleApp(ac.getContainerId(), ac.getRemainingThroughput(), ac.getTargetLatency());
//            costMap.put(ac.getContainerId(), ra.getTotalCost());
//            logger.info("Current original cost for {} is {} ", ac.getContainerId(), ra.getTotalCost());
//        }
//        
//        // 2. calculate the current remaining fit
//        logger.info("Step 2. Calculate the remaining fit");
//        Map<String, ApplicationAllocation> remainingFit = new HashMap<String, ApplicationAllocation>();
//        for(ApplicationCandidate ac : candidates) {
//            if (ac.isDone()) continue;
//            
//            String instanceType = ir.getInstanceType();
//            AppPerformanceRecord record = getAnalysisRecordsData(ac.getContainerId(), instanceType);
//            if (record == null) {
//                return null;
//            }
//            ApplicationAllocation aa = record.getPredictor().predictThroughput(
//                    ir.getRemainingCpu(), ir.getRemainingMem(), null, null, 
//                    ac.getTargetThroughput(), ac.getTargetLatency());
//            if (aa == null) {                    
//                aa = new ApplicationAllocation();
//                aa.setAllocatedThroughput(0);
//            }
//            aa.setContainerId(ac.getContainerId());
//            aa.setMinLatency(ac.getTargetLatency());
//            remainingFit.put(ac.getContainerId(), aa);
//            logger.info("Remaining fit for {} is {}", ac.getContainerId(), aa.getAllocatedThroughput());
//            
//        }
//        
//        // 3. calculate the new total cost after the current remaining fit
//        // 4. calculate the savings (1. - 3.)
//        logger.info("Step 3. Calculate the savings");
//        for(ApplicationCandidate ac : candidates) {
//            if (ac.isDone()) continue;
//            int savedThroughput = remainingFit.get(ac.getContainerId()).getAllocatedThroughput();
//            ResourceAllocation ra = 
//                    getSolutionForSingleApp(ac.getContainerId(), 
//                            ac.getRemainingThroughput() - savedThroughput, ac.getTargetLatency());
//            
//            Double originalCost = costMap.get(ac.getContainerId());
//            Double savedCost = originalCost - ra.getTotalCost();
//            costMap.put(ac.getContainerId(), savedCost);
//        }
//        
//        // 5. sort the savings
//        costMap = sortByComparator(costMap);
//        String chosenContainerId = null;
//        for (Map.Entry<String, Double> entry : costMap.entrySet()) {
//            logger.info("Savings for {} is {}", entry.getKey(), entry.getValue());
//            chosenContainerId = entry.getKey();
//        }
//        
//        // 6. return the biggest savings
//        if (chosenContainerId == null) {
//            return null;
//        } else {
//            for(ApplicationCandidate ac : candidates) {
//                if (ac.getContainerId().equals(chosenContainerId)) {
//                    return ac;
//                }
//            }
//            return null;
//        }
        return null;
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
}