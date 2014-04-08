package io.magnum.jetty.server.data;

import java.util.ArrayList;
import java.util.List;

public class InstanceResource {

    public static double CPU_LIMIT = 0.95; // 95% 
    public static double MEM_LIMIT = 0.95; // 95% 
    
    private String instanceType;
    private List<ApplicationAllocation> allocatedApplications = new ArrayList<ApplicationAllocation>();
    
    public String getInstanceType() {
        return instanceType;
    }
    
    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }
    
    public List<ApplicationAllocation> getAllocatedApplications() {
        return allocatedApplications;
    }
    
    public void setAllocatedApplications(List<ApplicationAllocation> allocatedApplications) {
        this.allocatedApplications = allocatedApplications;
    }

    public double getCost() {        
        return Pricing.getCost(instanceType);
    }    
    
    public double getTotalCpu() {
        double sum = 0;
        for(ApplicationAllocation aa : allocatedApplications) {
            sum += aa.getCpu();
        }
        return sum;
    }
    
    public double getRemainingCpu() {
        return 100 - getTotalCpu();
    }
    
    public double getRemainingMem() {
        return 100 - getTotalMem();
    }
    
    public double getTotalMem() {
        double sum = 0;
        for(ApplicationAllocation aa : allocatedApplications) {
            sum += aa.getMem();
        }
        return sum;
    }
    
    public double getTotalNetwork() {
        double sum = 0;
        for(ApplicationAllocation aa : allocatedApplications) {
            sum += aa.getNetwork();
        }
        return sum;
    }
    
    public double getTotalDisk() {
        double sum = 0;
        for(ApplicationAllocation aa : allocatedApplications) {
            sum += aa.getDisk();
        }
        return sum;
    }
    
    public boolean hasRemaining() {
        return getTotalCpu() <= CPU_LIMIT && getTotalMem() <= MEM_LIMIT;
    }
}