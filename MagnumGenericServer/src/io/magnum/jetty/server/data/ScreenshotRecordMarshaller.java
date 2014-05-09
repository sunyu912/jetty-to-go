package io.magnum.jetty.server.data;

import io.magnum.jetty.server.url.JsonMapper;
import java.io.IOException;
import com.amazonaws.services.dynamodb.datamodeling.DynamoDBMarshaller;

public class ScreenshotRecordMarshaller implements
        DynamoDBMarshaller<ScreenshotRecord> {

    @Override
    public String marshall(ScreenshotRecord getterReturnResult) {
        try {
            return JsonMapper.mapper.writeValueAsString(getterReturnResult);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public ScreenshotRecord unmarshall(Class<ScreenshotRecord> clazz, String obj) {
        try {
            return JsonMapper.mapper.readValue(obj, clazz);
        } catch (IOException e) {
            return null;
        }
    }
}
