package io.magnum.jetty.server.data.analysis;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.ApplicationAllocation;
import io.magnum.jetty.server.data.CleanedThroughputRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinearSlopePredictor extends ResourceThroughputPredictor {

    private static final Logger logger = LoggerFactory.getLogger(LinearSlopePredictor.class);
    
    public LinearSlopePredictor() {    
    }
    
    public LinearSlopePredictor(AppPerformanceRecord r) {
        super(r);
    }   

    private int getThroughputBasedCpu(CleanedThroughputRecord pre, CleanedThroughputRecord r, double cpu) {
        double cpuL = (r.getThroughput() - pre.getThroughput()) / (r.getCpu() - pre.getCpu());
        double cpuK = pre.getThroughput() - cpuL * pre.getCpu();
        return (int) (cpu * cpuL + cpuK);
    }
    
    private int getThroughputBasedMem(CleanedThroughputRecord pre, CleanedThroughputRecord r, double mem) {
        double memL = (r.getThroughput() - pre.getThroughput()) / (r.getMem() - pre.getMem());
        double memK = pre.getThroughput() - memL * pre.getMem();
        return (int) (mem * memL + memK);
    }
    
    private int getThroughputBasedNetwork(CleanedThroughputRecord pre, CleanedThroughputRecord r, double network) {
        double networkL = (r.getThroughput() - pre.getThroughput()) / (r.getNetwork() - pre.getNetwork());
        double networkK = pre.getThroughput() - networkL * pre.getCpu();
        return (int) (network * networkL + networkK);
    }
    
    @Override
    public ApplicationAllocation predictThroughput(Double cpu, Double mem,
            Double network, Double disk, Integer throughput, Double latency) {
        logger.info("Predicting throughput {} based on resource CPU {} Mem {} Network {} Latency {}",
                throughput, cpu, mem, network, latency);
        
        if (latency == null) {
            latency = Double.MAX_VALUE;
        }
        CleanedThroughputRecord pre = null;
        for(CleanedThroughputRecord r : getRecord().getOrderredThroughputDataList()) {
            logger.info("Data point CPU {} Mem {} Network {} Latency {} Throughput {}",
                    r.getCpu(), r.getMem(), r.getNetwork(), r.getLatency(), r.getThroughput());
            if (cpu >= r.getCpu() && mem >= r.getMem() && latency >= r.getLatency() && throughput > r.getThroughput()) {
                logger.info("Good point");
                pre = r;
            } else {
                logger.info("Stopping point");
                if (pre == null) {
                    if (cpu < r.getCpu() || mem < r.getMem() || latency < r.getLatency()) {
                        logger.info("No enough for the lowest resource. Stop here. No allocation");
                        return null;
                    } else {
                        logger.info("First data point is enough for the throughput and all resources is good.");
                        ApplicationAllocation aa = new ApplicationAllocation();
                        aa.setAllocatedThroughput(throughput);
                        aa.setCpu(r.getCpu());
                        aa.setMem(r.getMem());
                        aa.setNetwork(r.getNetwork());
                        
                        logger.info("Allocation CPU {} MEM {} Network {} Throughput {}", aa.getCpu(),
                                aa.getMem(), aa.getNetwork(), aa.getAllocatedThroughput());
                        return aa;
                    }                                        
                }                                
                
                int throughputCpu = Integer.MAX_VALUE;
                if (cpu < r.getCpu()) {
                    logger.info("CPU bounded");
                    throughputCpu = getThroughputBasedCpu(pre, r, cpu);
                }                
                int throughputMem = Integer.MAX_VALUE;
                if (mem < r.getMem()) {
                    logger.info("MEM bounded");
                    throughputMem = getThroughputBasedMem(pre, r, mem);
                }
                if (latency < r.getLatency()) {
                    // use pre
                    logger.info("Latency bounded. Use previous safe data point");
                    int actualThroughput = Math.min(pre.getThroughput(), throughput);
                    ApplicationAllocation aa = new ApplicationAllocation();
                    aa.setAllocatedThroughput(actualThroughput);
                    aa.setCpu(pre.getCpu());
                    aa.setMem(pre.getMem());
                    aa.setNetwork(pre.getNetwork());
                    
                    logger.info("Allocation CPU {} MEM {} Network {} Throughput {}", aa.getCpu(),
                            aa.getMem(), aa.getNetwork(), aa.getAllocatedThroughput());
                    return aa;
                }
                                
                int minThroughput = Math.min(throughputCpu, throughputMem); 
                int actualThroughput = Math.min(minThroughput, throughput); // 
                
                ApplicationAllocation aa = new ApplicationAllocation();
                aa.setAllocatedThroughput(actualThroughput);
                aa.setCpu(getCpu(pre, r, actualThroughput));
                aa.setMem(getMem(pre, r, actualThroughput));
                aa.setNetwork(getNetwork(pre, r, actualThroughput));
                
                logger.info("Allocation CPU {} MEM {} Network {} Throughput {}", aa.getCpu(),
                        aa.getMem(), aa.getNetwork(), aa.getAllocatedThroughput());
                return aa;
            }
        }
        
        logger.info("All data points are good. Take the maximum allocation");
        ApplicationAllocation aa = new ApplicationAllocation();
        CleanedThroughputRecord rr = getRecord().getOrderredThroughputDataList().get(getRecord().getOrderredThroughputDataList().size() - 1);
        aa.setCpu(rr.getCpu());
        aa.setMem(rr.getMem());
        aa.setNetwork(rr.getNetwork());
        aa.setAllocatedThroughput(rr.getThroughput());
        logger.info("Allocation CPU {} MEM {} Network {} Throughput {}", aa.getCpu(),
                aa.getMem(), aa.getNetwork(), aa.getAllocatedThroughput());
        return aa;
    }

    @Override
    public ApplicationAllocation predictResource(Integer throughput, Double latency) {
        CleanedThroughputRecord pre = null;
        for(CleanedThroughputRecord r : getRecord().getOrderredThroughputDataList()) {
            if (throughput >= r.getThroughput() && latency >= r.getLatency()) {
                pre = r;
            } else {
                if (pre == null) {
                    if (latency < r.getLatency()) {
                        return null;
                    }
                    ApplicationAllocation a = new ApplicationAllocation();
                    a.setAllocatedThroughput(throughput);                
                    a.setCpu(r.getCpu());
                    a.setMem(r.getMem());
                    a.setNetwork(r.getNetwork());
                    return a;
                }
                ApplicationAllocation a = new ApplicationAllocation();
                a.setAllocatedThroughput(throughput);                
                a.setCpu(getCpu(pre, r, throughput));
                a.setMem(getMem(pre, r, throughput));
                a.setNetwork(getNetwork(pre, r, throughput));                
                return a;
            }
        }                
        if (pre != null) {
            ApplicationAllocation a = new ApplicationAllocation();
            a.setAllocatedThroughput(pre.getThroughput());                
            a.setCpu(pre.getCpu());
            a.setMem(pre.getMem());
            a.setNetwork(pre.getNetwork());
            return a;
        }
        return null;
    }

    @Override
    public CleanedThroughputRecord predictResourceInCTR(Integer throughput, Double latency) {
        CleanedThroughputRecord pre = null;
        for(CleanedThroughputRecord r : getRecord().getOrderredThroughputDataList()) {
            if (throughput >= r.getThroughput() && latency >= r.getLatency()) {
                pre = r;
            } else {
                if (pre == null) {
                    if (latency < r.getLatency()) {
                        return null;
                    }
                    return r;
                }
                
                CleanedThroughputRecord res = new CleanedThroughputRecord();                
                res.setThroughput(throughput);
                res.setCpu(getCpu(pre, r, throughput));
                res.setNetwork(getNetwork(pre, r, throughput));
                res.setMem(getMem(pre, r, throughput));
                return res;
            }
        }
        return null;
    }
    
    private double getCpu(CleanedThroughputRecord pre, CleanedThroughputRecord r, Integer throughput) {
        double cpuL = (r.getThroughput() - pre.getThroughput()) / (r.getCpu() - pre.getCpu());
        double cpuK = pre.getThroughput() - cpuL * pre.getCpu();
        return ((throughput - cpuK) / cpuL);
    }
    
    private double getMem(CleanedThroughputRecord pre, CleanedThroughputRecord r, Integer throughput) {
        double memL = (r.getThroughput() - pre.getThroughput()) / (r.getMem() - pre.getMem());
        double memK = pre.getThroughput() - memL * pre.getMem();
        return ((throughput - memK) / memL);
    }
    
    private double getNetwork(CleanedThroughputRecord pre, CleanedThroughputRecord r, Integer throughput) {
        double networkL = (r.getThroughput() - pre.getThroughput()) / (r.getNetwork() - pre.getNetwork());
        double networkK = pre.getThroughput() - networkL * pre.getNetwork();
        return ((throughput - networkK) / networkL);
    }
}
