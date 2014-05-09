package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.BatchRunHistoryRecord;
import io.magnum.jetty.server.data.BatchRunRecord;
import io.magnum.jetty.server.data.BatchRunResultRecord;
import io.magnum.jetty.server.data.ScreenshotRecord;

import java.util.List;


public interface DataProvider {
    
    public void addScreenshotRecord(ScreenshotRecord record);
    public List<ScreenshotRecord> listScreenshots(String url);
    public void addGenericObject(Object obj);
    public List<BatchRunRecord> listBatchRuns(String id);
    public List<BatchRunHistoryRecord> listBatchHistory(String id);
    public List<BatchRunResultRecord> listBatchRunResult(Long timestamp);
}
