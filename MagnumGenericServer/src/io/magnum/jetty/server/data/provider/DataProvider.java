package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.TestInfo;


public interface DataProvider {
    
    public TestInfo updateTestInfo(String id, String status);
    
    public TestInfo getTestInfo(String id);
}
