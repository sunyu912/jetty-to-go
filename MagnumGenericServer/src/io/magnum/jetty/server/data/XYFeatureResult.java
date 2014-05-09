package io.magnum.jetty.server.data;

import java.io.IOException;

import io.magnum.jetty.server.url.JsonMapper;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBIgnore;

public class XYFeatureResult {

    private Integer numOfLeaves;
    private Integer numOfTextGroup;
    private Integer numOfImageArea;
    private Integer maxDecompositionLevel;
    
    public Integer getNumOfLeaves() {
        return numOfLeaves;
    }
    
    public void setNumOfLeaves(Integer numOfLeaves) {
        this.numOfLeaves = numOfLeaves;
    }
    
    public Integer getNumOfTextGroup() {
        return numOfTextGroup;
    }
    
    public void setNumOfTextGroup(Integer numOfTextGroup) {
        this.numOfTextGroup = numOfTextGroup;
    }
    
    public Integer getNumOfImageArea() {
        return numOfImageArea;
    }
    
    public void setNumOfImageArea(Integer numOfImageArea) {
        this.numOfImageArea = numOfImageArea;
    }
    
    public Integer getMaxDecompositionLevel() {
        return maxDecompositionLevel;
    }
    
    public void setMaxDecompositionLevel(Integer maxDecompositionLevel) {
        this.maxDecompositionLevel = maxDecompositionLevel;
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
