package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.BenchmarkRecord;
import io.magnum.jetty.server.data.TestInfo;

import java.util.List;


public interface DataProvider {
    
    public TestInfo updateTestInfo(String id, String status);
    
    public TestInfo getTestInfo(String id);
    
    public List<BenchmarkRecord> listBenchmarkRecords(String id);
}
