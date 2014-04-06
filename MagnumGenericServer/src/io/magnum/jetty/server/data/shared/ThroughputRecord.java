package io.magnum.jetty.server.data.shared;

public class ThroughputRecord {

	private String label;
	private long startTime;
	private int count;        
	private int mean;
	private int errorCount;
	private int periodInSec;
	private String threads;

	public ThroughputRecord() {
		super();
	}

	public ThroughputRecord(String label, long startTime, int count, int mean,
			int errorCount, int periodInSec, String threads) {
		super();
		this.label = label;
		this.startTime = startTime;
		this.count = count;
		this.mean = mean;
		this.errorCount = errorCount;
		this.periodInSec = periodInSec;
		this.setThreads(threads);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getMean() {
		return mean;
	}

	public void setMean(int mean) {
		this.mean = mean;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public int getPeriodInSec() {
		return periodInSec;
	}

	public void setPeriodInSec(int periodInSec) {
		this.periodInSec = periodInSec;
	}

	public String getThreads() {
		return threads;
	}

	public void setThreads(String threads) {
		this.threads = threads;
	}	       
}
