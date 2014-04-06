package io.magnum.jetty.server.data.shared;


public class GlobalDataCollectorJsonWrapper {

	private ExpectedTimeThroughputMap expectedTimeThroughputMap;
	private TimeThroughputMap throughputMap;
	private HostPerfMap perfMap;	
	private int warmupDuration;
	private int duration;
	private int steps;
	private int targetThroughput;
	
	// processed fields
	private TimeThroughputMap capturedThroughputPoints;
	private HostPerfMap capturedPerfPoints;
	
	public GlobalDataCollectorJsonWrapper() {
		perfMap = new HostPerfMap();
		throughputMap = new TimeThroughputMap();
		capturedPerfPoints = new HostPerfMap();
        capturedThroughputPoints = new TimeThroughputMap();
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
}
