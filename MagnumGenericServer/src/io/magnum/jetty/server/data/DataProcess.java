package io.magnum.jetty.server.data;

import io.magnum.jetty.server.data.shared.GlobalDataCollectorJsonWrapper;
import io.magnum.jetty.server.util.JsonMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class DataProcess {
    
    public static void main(String[] args) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
        GlobalDataCollectorJsonWrapper data = JsonMapper.mapper.readValue(
                new URL("https://s3.amazonaws.com/roar-tests/e04eb90a-2157-4a25-9c3d-5b290e67cbb1/throughput-perm-processed.json"), 
                GlobalDataCollectorJsonWrapper.class);
        
        //data.outputData();
        //data.outputAllData();
        data.outputCapturedData();
    }
}
