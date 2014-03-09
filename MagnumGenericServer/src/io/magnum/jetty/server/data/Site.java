package io.magnum.jetty.server.data;

public class Site {

    private String id;
    private String name;
    private String url;
    private String type;
    private boolean status;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;    
    }
    
    public boolean isStatus() {
        return status;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
}
