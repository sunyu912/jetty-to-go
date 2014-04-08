package io.magnum.jetty.server.data;

public class CleanedThroughputRecord {

    private int throughput;
    private double latency;
    private double cpu;
    private double mem;
    private double network;
    private double disk;
    
    public int getThroughput() {
        return throughput;
    }
    
    public void setThroughput(int throughput) {
        this.throughput = throughput;
    }
    
    public double getLatency() {
        return latency;
    }
    
    public void setLatency(double latency) {
        this.latency = latency;
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
}
