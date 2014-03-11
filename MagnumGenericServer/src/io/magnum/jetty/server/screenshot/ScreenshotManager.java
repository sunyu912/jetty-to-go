package io.magnum.jetty.server.screenshot;

import io.magnum.jetty.server.data.ScreenshotRecord;

public interface ScreenshotManager {

    public ScreenshotRecord getScreenshot(String url);
    
}
