package io.magnum.jetty.server.data;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class ColocationTestRecord {
    
    private Long timestamp;    
    private String type;
    private List<CotestAppInfo> apps;
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<CotestAppInfo> getApps() {
        return apps;
    }
    
    public void setApps(List<CotestAppInfo> apps) {
        this.apps = apps;
    }
    
    @JsonIgnore
    public CotestAppInfo getTargetApp() {
        for(CotestAppInfo app : apps) {
            if (app.isTarget()) {
                return app;
            }
        }
        return null;
    }
    
    @JsonIgnore
    public CotestAppInfo getAppByName(String containerId) {
        for(CotestAppInfo app : apps) {
            if (app.getContainerId().equals(containerId)) {
                return app;
            }
        }
        return null;
    }
}