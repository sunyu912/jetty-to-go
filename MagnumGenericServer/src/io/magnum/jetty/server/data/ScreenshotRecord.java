package io.magnum.jetty.server.data;

public class ScreenshotRecord {

    private String url;
    private String imageS3Url;
    private Long timestamp;
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getImageS3Url() {
        return imageS3Url;
    }
    
    public void setImageS3Url(String imageS3Url) {
        this.imageS3Url = imageS3Url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }   
}
