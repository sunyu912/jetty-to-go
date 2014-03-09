package io.magnum.jetty.server.data;

import java.util.Arrays;

public class AppConfigLoader {

    private static AppConfig appConfig;
    
    public static AppConfig getAppConfig() {
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setInterval(1000);
            appConfig.setRule("hd4ar rules");
            appConfig.setEndpoints(Arrays.asList(new String[]{"http://us-east-1", "http://us-east-2", "http://us-west-1", "http://us-west-2"}));
        }
        return appConfig;
    }

}
