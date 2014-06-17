package io.magnum.jetty.server.data.shared;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;


public class GlobalDataCollectorJsonWrapper {

	private ExpectedTimeThroughputMap expectedTimeThroughputMap;
	private TimeThroughputMap throughputMap;
	private HostPerfMap perfMap;	
	private int warmupDuration;
	private int duration;
	private int steps;
	private int targetThroughput;
	private int peakThroughput;
	
	// processed fields
	private TimeThroughputMap capturedThroughputPoints;
	private HostPerfMap capturedPerfPoints;
	private Map<String, Boolean> hostFullnessMap;
	
	public GlobalDataCollectorJsonWrapper() {
		perfMap = new HostPerfMap();
		throughputMap = new TimeThroughputMap();
		capturedPerfPoints = new HostPerfMap();
        capturedThroughputPoints = new TimeThroughputMap();
        hostFullnessMap = new HashMap<String, Boolean>();
	}
	
	public GlobalDataCollectorJsonWrapper(HostPerfMap perfMap,
			TimeThroughputMap throughputMap) {
		this.perfMap = perfMap;
		this.throughputMap = throughputMap;
	}

	public TimeThroughputMap getThroughputMap() {
		return throughputMap;
	}
	
	public void setThroughputMap(TimeThroughputMap throughputMap) {
		this.throughputMap = throughputMap;
	}
	
	public HostPerfMap getPerfMap() {
		return perfMap;
	}
	
	public void setPerfMap(HostPerfMap perfMap) {
		this.perfMap = perfMap;
	}

	public ExpectedTimeThroughputMap getExpectedTimeThroughputMap() {
		return expectedTimeThroughputMap;
	}

	public void setExpectedTimeThroughputMap(ExpectedTimeThroughputMap expectedTimeThroughputMap) {
		this.expectedTimeThroughputMap = expectedTimeThroughputMap;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public int getWarmupDuration() {
		return warmupDuration;
	}

	public void setWarmupDuration(int warmupDuration) {
		this.warmupDuration = warmupDuration;
	}

	public int getTargetThroughput() {
		return targetThroughput;
	}

	public void setTargetThroughput(int targetThroughput) {
		this.targetThroughput = targetThroughput;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

    public TimeThroughputMap getCapturedThroughputPoints() {
        return capturedThroughputPoints;
    }

    public void setCapturedThroughputPoints(TimeThroughputMap capturedThroughputPoints) {
        this.capturedThroughputPoints = capturedThroughputPoints;
    }

    public HostPerfMap getCapturedPerfPoints() {
        return capturedPerfPoints;
    }

    public void setCapturedPerfPoints(HostPerfMap capturedPerfPoints) {
        this.capturedPerfPoints = capturedPerfPoints;
    }
    
    @JsonIgnore
    public void outputCapturedData() {
        List<ThroughputRecord> throughputList = capturedThroughputPoints.getRecordList();
        List<PerfRecord> perfList = capturedPerfPoints.getPerfList();
        
        assert(throughputList.size() == perfList.size());
        
        File cpuInput = new File("/Users/yusun/Desktop/cpu.arff");
        try {
            FileWriter writer = new FileWriter(cpuInput);
            writer.write("@relation 'throughput'");
            writer.write("\n");
            writer.write("@attribute CPU numeric");
            writer.write("\n");
//            writer.write("@attribute CPU2 numeric");
//            writer.write("\n");
//            writer.write("@attribute CPU3 numeric");
//            writer.write("\n");
            writer.write("@attribute throughput numeric");
            writer.write("\n");
            writer.write("@data");
            writer.write("\n");
            
            for(int i = 0; i < perfList.size(); i++) {
                double cpu = perfList.get(i).getCpu();
                double cpu2 = cpu * cpu;
                double cpu3 = cpu * cpu * cpu;
                int thougput = throughputList.get(i).getCount();
                //writer.write(cpu+","+cpu2+","+cpu3+","+thougput);
                writer.write(cpu+","+thougput);
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @JsonIgnore
    public void outputAllData() {
        List<ThroughputRecord> throughputList = throughputMap.getRecordList();
        List<PerfRecord> perfList = perfMap.getPerfList();
        
        assert(throughputList.size() == perfList.size());
        
        File cpuInput = new File("/Users/yusun/Desktop/cpu.arff");
        try {
            FileWriter writer = new FileWriter(cpuInput);
            writer.write("@relation 'throughput'");
            writer.write("\n");
            writer.write("@attribute CPU numeric");
            writer.write("\n");
            writer.write("@attribute CPU2 numeric");
            writer.write("\n");
            writer.write("@attribute CPU3 numeric");
            writer.write("\n");
            writer.write("@attribute CPU4 numeric");
            writer.write("\n");
            writer.write("@attribute throughput numeric");
            writer.write("\n");
            writer.write("@data");
            writer.write("\n");
            
            for(int i = 25; i < perfList.size(); i++) {
                
                double cpu = 100 - perfList.get(i).getCpuIdle();
                double cpu2 = cpu * cpu;
                double cpu3 = cpu * cpu * cpu;
                double cpu4 = cpu * cpu * cpu * cpu;
                int thougput = throughputList.get(i).getCount();
                writer.write(cpu+","+cpu2+","+cpu3+","+cpu4+","+thougput);
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPeakThroughput() {
        return peakThroughput;
    }

    public void setPeakThroughput(int peakThroughput) {
        this.peakThroughput = peakThroughput;
    }

    public Map<String, Boolean> getHostFullnessMap() {
        return hostFullnessMap;
    }

    public void setHostFullnessMap(Map<String, Boolean> hostFullnessMap) {
        this.hostFullnessMap = hostFullnessMap;
    }
}
