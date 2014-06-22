package io.magnum.jetty.server.data;

import java.util.HashMap;
import java.util.Map;

public class Pricing {

    private static final Map<String, Double> PRCIING = 
            new HashMap<String, Double>() {{
                put("m1.small", 0.044);
                put("m1.medium", 0.087);
                put("m1.large", 0.175);
                put("m1.xlarge", 0.35);
                put("m3.medium", 0.07);
                put("m3.large", 0.14);
                put("m3.xlarge", 0.28);
                put("m3.2xlarge", 0.56);
                put("m2.xlarge", 0.245);
                put("m2.2xlarge", 0.49);
                put("m2.4xlarge", 0.98);
                put("cr1.8xlarge", 3.5);
                put("c3.large", 0.105);
                put("c3.xlarge", 0.21);
                put("c3.2xlarge", 0.42);
                put("c3.4xlarge", 0.84);
                put("c3.8xlarge", 1.68);                
                put("c1.medium", 0.13);
                put("c1.xlarge", 0.52);
                put("cc2.8xlarge", 2.0);
                put("g2.2xlarge", 0.65);
                put("t1.micro", 0.02);
                put("f1-micro", 0.014);
                put("g1-small", 0.0385);
                put("n1-standard-1", 0.077);
                put("n1-standard-2", 0.154);
                put("n1-standard-4", 0.308);
                put("n1-standard-8", 0.616);
                put("n1-standard-16", 1.232);
                put("n1-highmem-2", 0.18);
                put("n1-highmem-4", 0.36);
                put("n1-highmem-8", 0.72);
                put("n1-highmem-16", 1.44);
                put("n1-highcpu-2", 0.096);
                put("n1-highcpu-4", 0.192);
                put("n1-highcpu-8", 0.384);
                put("n1-highcpu-16", 0.768);
            }};
    
    public static Double getCost(String instanceType) {
        Double price = PRCIING.get(instanceType);
        return price * 10000;
    }
    
    public static Double getCostPerThroughput(String instanceType, int throughput) {
        Double price = PRCIING.get(instanceType);
        if (price != null && throughput > 0) {
            return price * 10000 / throughput;
        }
        return null;
    }
}
