package io.magnum.jetty.server.data;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="ScreenshotRecord")
public class ScreenshotRecord {

    private String url;
    private String imageS3Url;
    private String id;
    private Long timestamp;
    private boolean isSuccess;
    
    @DynamoDBRangeKey
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
    
    @DynamoDBHashKey
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }   
}
