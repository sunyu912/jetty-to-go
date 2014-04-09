package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.AppPerformanceRecord;
import io.magnum.jetty.server.data.BenchmarkRecord;
import io.magnum.jetty.server.data.TestInfo;

import java.util.List;
import java.util.Map;


public interface DataProvider {
    
    public TestInfo updateTestInfo(String id, String status);
    
    public TestInfo getTestInfo(String id);
    
    public List<BenchmarkRecord> listBenchmarkRecords(String id);
    
    public List<AppPerformanceRecord> listAppPerformanceRecord(String containerId);
    
    public void updateGeneric(Object obj);
    
    public Map<String, Integer> getAvailableApps();
}
