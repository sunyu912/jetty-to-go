package io.magnum.jetty.server.data;

import io.magnum.jetty.server.util.JsonMapper;

import java.io.IOException;
import java.util.Set;

import org.codehaus.jackson.type.TypeReference;

import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMarshaller;

public class CleanedThroughputRecordListMarshaller implements
        DynamoDBMarshaller<Set<CleanedThroughputRecord>> {

    @Override
    public String marshall(Set<CleanedThroughputRecord> getterReturnResult) {
        String res = null;
        try {            
            System.out.println(JsonMapper.mapper.writeValueAsString(getterReturnResult));
            res = JsonMapper.mapper.writeValueAsString(getterReturnResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Set<CleanedThroughputRecord> unmarshall(
            Class<Set<CleanedThroughputRecord>> clazz, String obj) {
        Set<CleanedThroughputRecord> res = null;
        try {
            res = JsonMapper.mapper.readValue(obj, new TypeReference<Set<CleanedThroughputRecord>>() {});
        } catch (IOException e) {         
            e.printStackTrace();
        }
        return res;
    }
}
