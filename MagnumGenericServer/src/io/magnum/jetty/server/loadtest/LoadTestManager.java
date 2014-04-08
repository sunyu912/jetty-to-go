package io.magnum.jetty.server.loadtest;

import java.io.InputStream;

public interface LoadTestManager {

    public String runTest(String testId, InputStream testPlanInput, String containerId, String instanceType);
    
    public void postProcessingData(String testId, String containerId, String instanceType);
}
