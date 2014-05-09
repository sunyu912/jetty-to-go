package io.magnum.jetty.server.data;

import io.magnum.jetty.server.url.JsonMapper;
import java.io.IOException;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMarshaller;

public class AnalysisResultMarshaller implements
        DynamoDBMarshaller<AnalysisResult> {

    @Override
    public String marshall(AnalysisResult getterReturnResult) {
        try {
            return JsonMapper.mapper.writeValueAsString(getterReturnResult);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public AnalysisResult unmarshall(Class<AnalysisResult> clazz, String obj) {
        try {
            return JsonMapper.mapper.readValue(obj, clazz);
        } catch (IOException e) {
            return null;
        }
    }
}
