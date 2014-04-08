package io.magnum.jetty.server.data;

public class ApplicationCandidate {

    private String containerId;
    private Integer targetThroughput;
    private Double targetLatency;
    private Integer remainingThroughput;
    private AppPerformanceRecord currentFirstChoice;
    
    public String getContainerId() {
        return containerId;
    }
    
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
    
    public Integer getTargetThroughput() {
        return targetThroughput;
    }
    
    public void setTargetThroughput(Integer targetThroughput) {
        this.targetThroughput = targetThroughput;
        this.remainingThroughput = targetThroughput;
    }
    
    public Double getTargetLatency() {
        return targetLatency;
    }
    
    public void setTargetLatency(Double targetLatency) {
        this.targetLatency = targetLatency;
    }
    
    public Integer getRemainingThroughput() {
        return remainingThroughput;
    }
    
    public void setRemainingThroughput(Integer remainingThroughput) {
        this.remainingThroughput = remainingThroughput;
    }
    
    public boolean isDone() {
        return remainingThroughput <= 0;
    }

    public AppPerformanceRecord getCurrentFirstChoice() {
        return currentFirstChoice;
    }

    public void setCurrentFirstChoice(AppPerformanceRecord currentFirstChoice) {
        this.currentFirstChoice = currentFirstChoice;
    }
}
