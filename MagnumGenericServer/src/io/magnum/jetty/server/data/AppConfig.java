package io.magnum.jetty.server.data;

import java.util.List;

public class AppConfig {

    private List<String> endpoints;
    private long interval;
    private String rule;
    
    public List<String> getEndpoints() {
        return endpoints;
    }
    
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
    
    public long getInterval() {
        return interval;
    }
    
    public void setInterval(long interval) {
        this.interval = interval;
    }
    
    public String getRule() {
        return rule;
    }
    
    public void setRule(String rule) {
        this.rule = rule;
    }
}
