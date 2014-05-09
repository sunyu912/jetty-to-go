package io.magnum.jetty.server.url;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonMapper {
    
    public static final ObjectMapper mapper = new ObjectMapper() {{
        configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }};
    
}