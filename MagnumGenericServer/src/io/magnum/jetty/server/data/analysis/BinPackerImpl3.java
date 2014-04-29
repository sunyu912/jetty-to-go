package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.ApplicationCandidate;
import io.magnum.jetty.server.data.InstanceResource;
import io.magnum.jetty.server.data.Pricing;
import io.magnum.jetty.server.data.provider.DataProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BinPackerImpl3 extends BinPacker {

    public BinPackerImpl3(DataProvider provider, boolean enableCotest) {
        super(provider, enableCotest);
        setAlgName("Non-Sort Item Biggest Item / Non-Sort Bin");
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
//        
//        // choose the biggest one to allocate
//        Collections.sort(candidates, new ApplicationCandidateComparatorBasedOnInstancePrice());
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

    @Override
    InstanceResource handleVerifiedFailure(ApplicationCandidate firstCandidate,
            ApplicationAllocation aa) {
//        InstanceResource ir = new InstanceResource();
//        ir.setInstanceType(firstCandidate.getCurrentFirstChoice().getInstanceType());
//        System.out.println("YUSUNTEST: " + aa.getContainerId() + " - " + aa.getAllocatedThroughput() + firstCandidate.getCurrentFirstChoice().getInstanceType());
//        ApplicationAllocation a = firstCandidate.getCurrentFirstChoice().getPredictor().predictResource(aa.getAllocatedThroughput(), firstCandidate.getTargetLatency());
//        
//        a.setContainerId(firstCandidate.getContainerId());
//        firstCandidate.setRemainingThroughput(firstCandidate.getRemainingThroughput() - a.getAllocatedThroughput());
//        ir.getAllocatedApplications().add(a);
//        return ir;
        return null;
    }

    @Override
    ApplicationCandidate swtichFirstCandidate(List<ApplicationCandidate> candidates,
            InstanceResource ir) {
        return null;        
    }
}