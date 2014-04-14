package io.magnum.jetty.server.data;

import io.magnum.jetty.server.data.shared.GlobalDataCollectorJsonWrapper;

public class CotestAppInfo {

    private String containerId;
    private boolean isTarget;
    private int throughput;
    private int step;
    private int cpu;
    private Double mem;
    private String testId;
    private GlobalDataCollectorJsonWrapper result;
    
    public String getContainerId() {
        return containerId;
    }
    
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
    
    public boolean isTarget() {
        return isTarget;
    }
    
    public void setTarget(boolean isTarget) {
        this.isTarget = isTarget;
    }
    
    public int getThroughput() {
        return throughput;
    }
    
    public void setThroughput(int throughput) {
        this.throughput = throughput;
    }
    
    public int getStep() {
        return step;
    }
    
    public void setStep(int step) {
        this.step = step;
    }
    
    public Double getMem() {
        return mem;
    }
    
    public void setMem(Double mem) {
        this.mem = mem;
    }
    
    public String getTestId() {
        return testId;
    }
    
    public void setTestId(String testId) {
        this.testId = testId;
    }
    
    public GlobalDataCollectorJsonWrapper getResult() {
        return result;
    }
    
    public void setResult(GlobalDataCollectorJsonWrapper result) {
        this.result = result;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }    
}
