package io.magnum.jetty.server.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrendingSites {    
    
    private static List<Site> trendingSites; 
    
    public static List<Site> getTrendingSites() {
        if (trendingSites == null) {
            trendingSites = new ArrayList<Site>();
            for(int i = 0; i < 10; i++) {
                Site site = new Site();
                site.setId(UUID.randomUUID().toString());
                site.setName("test" + i);
                site.setType("sift");
                site.setUrl("http://" + UUID.randomUUID().toString() + ".com");
                site.setStatus(false);
                trendingSites.add(site);
            }
        }
        return trendingSites;
    }
}
