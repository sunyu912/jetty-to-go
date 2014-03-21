package io.magnum.jetty.server.data.provider;

import io.magnum.jetty.server.data.ScreenshotRecord;

import java.util.List;


public interface DataProvider {
    
    public void addScreenshotRecord(ScreenshotRecord record);
    public List<ScreenshotRecord> listScreenshots(String url);
}
