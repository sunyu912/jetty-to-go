package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.InstanceResource;
import io.magnum.jetty.server.data.Pricing;
import io.magnum.jetty.server.data.provider.DataProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BinPackerImpl1 extends BinPacker {

    public BinPackerImpl1(DataProvider provider) {
        super(provider);
    }

    @Override
    void sortApplicationCandidates(List<ApplicationCandidate> candidates) {
                
        // fit the best choice for each
        for(ApplicationCandidate candidate : candidates) {
            List<AppPerformanceRecord> orderredChoices = 
                    orderAppRecordsBasedOnCost(candidate.getContainerId(), candidate.getRemainingThroughput(), candidate.getTargetLatency());
            candidate.setCurrentFirstChoice(orderredChoices.get(0));
            logger.info("TEST: set first can: " + orderredChoices.get(0));
        }
        
        // choose the biggest one to allocate
        Collections.sort(candidates, new ApplicationCandidateComparatorBasedOnInstancePrice());
    }

    @Override
    void sortBins(List<InstanceResource> bins) {
        
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
}