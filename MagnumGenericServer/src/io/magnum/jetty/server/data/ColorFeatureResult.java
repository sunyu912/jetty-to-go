package io.magnum.jetty.server.data;

import io.magnum.jetty.server.url.JsonMapper;

import java.io.IOException;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;

public class ColorFeatureResult {
    
    private Double colorfulness1;
    private Double colorfulness2;
    
    public Double getColorfulness1() {
        return colorfulness1;
    }
    
    public void setColorfulness1(Double colorfulness1) {
        this.colorfulness1 = colorfulness1;
    }
    
    public Double getColorfulness2() {
        return colorfulness2;
    }
    
    public void setColorfulness2(Double colorfulness2) {
        this.colorfulness2 = colorfulness2;
    }
    
    @JsonIgnore
    @DynamoDBIgnore
    public String toString() {
        try {
            return JsonMapper.mapper.writeValueAsString(this);
        } catch (IOException e) {
            return "N/A";
        }
    }
}