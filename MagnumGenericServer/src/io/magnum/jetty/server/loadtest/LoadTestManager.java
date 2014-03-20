package io.magnum.jetty.server.loadtest;

import java.io.InputStream;

public interface LoadTestManager {

    public String runTest(InputStream testPlanInput);
    
}
