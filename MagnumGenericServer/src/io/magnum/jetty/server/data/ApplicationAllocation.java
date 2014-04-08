package io.magnum.jetty.server.data;

public class ApplicationAllocation {

    private String containerId;
    private int allocatedThroughput;
    private double cpu;
    private double mem;
    private double network;
    private double disk;
    
    public String getContainerId() {
        return containerId;
    }
    
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
    
    public int getAllocatedThroughput() {
        return allocatedThroughput;
    }
    
    public void setAllocatedThroughput(int allocatedThroughput) {
        this.allocatedThroughput = allocatedThroughput;
    }
    
    public double getCpu() {
        return cpu;
    }
    
    public void setCpu(double cpu) {
        this.cpu = cpu;    
    }
    
    public double getMem() {
        return mem;
    }
    
    public void setMem(double mem) {
        this.mem = mem;
    }
    
    public double getNetwork() {
        return network;
    }
    
    public void setNetwork(double network) {
        this.network = network;
    }
    
    public double getDisk() {
        return disk;
    }
    
    public void setDisk(double disk) {
        this.disk = disk;
    }    
    
    public boolean isAllocated() {
        return allocatedThroughput > 0;
    }
}
