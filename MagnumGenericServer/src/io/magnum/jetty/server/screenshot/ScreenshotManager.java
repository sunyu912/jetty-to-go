package io.magnum.jetty.server.screenshot;

import io.magnum.jetty.server.data.BatchRunHistoryRecord;
import io.magnum.jetty.server.data.ScreenshotRecord;

import java.io.InputStream;

public interface ScreenshotManager {

    public ScreenshotRecord getScreenshot(String url, boolean enableAnalyze);
    
    public String addBatchRun(String description, InputStream in);
    
    public BatchRunHistoryRecord batchRun(String id);
}
