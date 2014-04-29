package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.InstanceResource;
import io.magnum.jetty.server.data.provider.DataProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BinPackerImpl2 extends BinPacker {

    public BinPackerImpl2(DataProvider provider, boolean enableCotest) {
        super(provider, enableCotest);
        setAlgName("Sort Item Biggest Item / Non-Sort Bin");
    }
    
    @Override
    protected boolean isCotestAllowed() {
        return false;
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
        Collections.sort(candidates, new ApplicationCandidateComparatorBasedTotalCostLeft());
    }

    @Override
    void sortBins(List<InstanceResource> bins) {
        // TODO Auto-generated method stub

    }
    
    private class ApplicationCandidateComparatorBasedTotalCostLeft implements Comparator<ApplicationCandidate> {
        @Override
        public int compare(ApplicationCandidate o1, ApplicationCandidate o2) {
            assert(o1.getCurrentFirstChoice() != null);
            assert(o2.getCurrentFirstChoice() != null);
            double cost1 = o1.getCurrentFirstChoice().getCostAtPeak() * o1.getRemainingThroughput();
            double cost2 = o1.getCurrentFirstChoice().getCostAtPeak() * o2.getRemainingThroughput();                        
            return (int) (cost2 - cost1);
        }        
    }

    @Override
    InstanceResource handleVerifiedFailure(ApplicationCandidate firstCandidate,
            ApplicationAllocation aa) {
        return null;
    }

    @Override
    ApplicationCandidate swtichFirstCandidate(List<ApplicationCandidate> candidates,
            InstanceResource ir) {
        return null;        
    }
}