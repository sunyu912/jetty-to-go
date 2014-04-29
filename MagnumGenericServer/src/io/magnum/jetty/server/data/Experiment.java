package io.magnum.jetty.server.data;

import io.magnum.jetty.server.data.shared.GlobalDataCollectorJsonWrapper;
import io.magnum.jetty.server.data.shared.PerfRecord;
import io.magnum.jetty.server.data.shared.ThroughputRecord;
import io.magnum.jetty.server.util.JsonMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class Experiment {

    /**
     * @param args
     * @throws IOException 
     * @throws MalformedURLException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    public static void main(String[] args) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
        
        GlobalDataCollectorJsonWrapper data = JsonMapper.mapper.readValue(
                new URL("https://s3.amazonaws.com/roar-tests/5a0e6db0-9281-4b09-aaa2-c671c8bb25c7/throughput-perm-processed.json"), GlobalDataCollectorJsonWrapper.class);
        List<ThroughputRecord> throughputList = data.getCapturedThroughputPoints().getRecordList();
        List<PerfRecord> perfList = data.getCapturedPerfPoints().getPerfList();
        
        int start = 3000;
        while (start < 20000) {
            int i = getR(throughputList, start);
            
            System.out.println("for tp " + start + " : cpu " + getValue(
                    throughputList.get(i).getCount(), throughputList.get(i+1).getCount(),
                    start, 
                    perfList.get(i).getCpu(), perfList.get(i + 1).getCpu()));
            
            System.out.println("for tp " + start + " : cpu " + getValue(
                    perfList.get(i).getCpu(), perfList.get(i + 1).getCpu(),                    
                    22, 
                    throughputList.get(i).getCount(), throughputList.get(i+1).getCount()));
            
//            System.out.println("for tp " + start + " : mem " + getValue(
//                    throughputList.get(i).getCount(), throughputList.get(i+1).getCount(),
//                    start, 
//                    perfList.get(i).getMemory(), perfList.get(i + 1).getMemory()));
//            System.out.println("for tp " + start + " : network " + getValue(
//                    throughputList.get(i).getCount(), throughputList.get(i+1).getCount(),
//                    start, 
//                    perfList.get(i).getNetworkEth0(), perfList.get(i + 1).getNetworkEth0()));
//            System.out.println("for tp " + start + " : disk " + getValue(
//                    throughputList.get(i).getCount(), throughputList.get(i+1).getCount(),
//                    start, 
//                    perfList.get(i).getDisk(), perfList.get(i + 1).getDisk()));
            
            start += 3000;
        }
    }
    
    
    
    private static int getR(List<ThroughputRecord> throughputList, int start) {
        for(int i = 0; i < throughputList.size(); i++) {
            if (throughputList.get(i).getCount() > start) {
                return i - 1;
            }
        }
        return -1;
    }



    private static double getValue(double tp1, double tp2, double tpx, double v1, double v2) {
        double cpuL = (tp2 - tp1) / (v2 - v1);
        double cpuK = tp1 - cpuL * v1;
        return ((tpx - cpuK) / cpuL);
    }

}
