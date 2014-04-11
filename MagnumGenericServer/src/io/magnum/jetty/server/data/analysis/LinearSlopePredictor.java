package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.CleanedThroughputRecord;

public class LinearSlopePredictor extends ResourceThroughputPredictor {

    public LinearSlopePredictor() {    
    }
    
    public LinearSlopePredictor(AppPerformanceRecord r) {
        super(r);
    }   

    @Override
    public ApplicationAllocation predictThroughput(Double cpu, Double mem,
            Double network, Double disk, Double latency) {        
        CleanedThroughputRecord pre = null;
        for(CleanedThroughputRecord r : getRecord().getOrderredThroughputDataList()) {
            if (cpu >= r.getCpu()) {
                pre = r;
            } else {
                if (pre == null) {
                    pre = new CleanedThroughputRecord();
                    pre.setCpu(0);
                    pre.setThroughput(0);
                    //return null;
                }
                double cpuL = (r.getThroughput() - pre.getThroughput()) / (r.getCpu() - pre.getCpu());
                double cpuK = pre.getThroughput() - cpuL * pre.getCpu();                  
                
                ApplicationAllocation aa = new ApplicationAllocation();
                aa.setAllocatedThroughput((int) (cpuL * cpu + cpuK));
                aa.setCpu(cpu);                
                return aa;
            }
        }
        System.out.println("SYHERE");
        ApplicationAllocation aa = new ApplicationAllocation();
        CleanedThroughputRecord rr = getRecord().getOrderredThroughputDataList().get(getRecord().getOrderredThroughputDataList().size() - 1);
        aa.setCpu(rr.getCpu());
        aa.setAllocatedThroughput(rr.getThroughput());
        aa.setCpu(cpu);
        return aa;
    }

    @Override
    public ApplicationAllocation predictResource(Integer throughput, Double latency) {
        CleanedThroughputRecord pre = null;
        for(CleanedThroughputRecord r : getRecord().getOrderredThroughputDataList()) {
            if (throughput >= r.getThroughput()) {
                pre = r;
            } else {
                if (pre == null) {
                    return null;
                }
                double cpuL = (r.getThroughput() - pre.getThroughput()) / (r.getCpu() - pre.getCpu());
                double cpuK = pre.getThroughput() - cpuL * pre.getCpu();                  
                
                ApplicationAllocation aa = new ApplicationAllocation();
                aa.setAllocatedThroughput(throughput);
                aa.setCpu((int) ((throughput - cpuK) / cpuL));                
                return aa;
            }
        }
        return null;
    }

    @Override
    public CleanedThroughputRecord predictResourceInCTR(Integer throughput,
            Double latency) {
        CleanedThroughputRecord pre = null;
        for(CleanedThroughputRecord r : getRecord().getOrderredThroughputDataList()) {
            if (throughput >= r.getThroughput()) {
                pre = r;
            } else {
                if (pre == null) {
                    pre = new CleanedThroughputRecord();
                    pre.setCpu(0);
                    pre.setThroughput(0);
                }
                double cpuL = (r.getThroughput() - pre.getThroughput()) / (r.getCpu() - pre.getCpu());
                double cpuK = pre.getThroughput() - cpuL * pre.getCpu();                  
                
                CleanedThroughputRecord res = new CleanedThroughputRecord();                
                res.setThroughput(throughput);
                res.setCpu((int) ((throughput - cpuK) / cpuL));                
                return res;
            }
        }
        return null;
    }
    
}
