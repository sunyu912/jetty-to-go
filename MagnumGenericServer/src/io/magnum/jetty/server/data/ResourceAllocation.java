package io.magnum.jetty.server.data;

import java.util.ArrayList;
import java.util.List;

public class ResourceAllocation {

    private List<InstanceResource> allocatedResources = new ArrayList<InstanceResource>();

    public List<InstanceResource> getAllocatedResources() {
        return allocatedResources;
    }

    public void setAllocatedResources(List<InstanceResource> allocatedResources) {
        this.allocatedResources = allocatedResources;
    }
    
    public double getTotalCost() {
        double res = 0;
        for(InstanceResource ir : allocatedResources) {
            res += ir.getCost();
        }
        return res;
    }
}
