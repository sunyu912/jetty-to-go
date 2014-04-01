package io.magnum.jetty.server.data.provider;

import java.util.List;


public interface DataProvider {
    
    public List<Integer> getPrimeNumbers(int limit);
    
}
