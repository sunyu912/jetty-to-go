package io.magnum.jetty.server.data.shared;

import org.codehaus.jackson.annotate.JsonIgnore;

public class PerfRecord {

	private double cpu;
	private double cpuIdle;
	private double cpuSteal;
	private double cpuIOWait;
	private double cpuSoftirq;
	private double memory;
	private double disk;
	private double network;
	private double networkEth0;
	
	public double getCpu() {
		return cpu;
	}
	
	public void setCpu(double cpu) {
		this.cpu = cpu;
	}
	
	public double getMemory() {
		return memory;
	}
	
	public void setMemory(double memory) {
		this.memory = memory;
	}		
	
	public double getNetwork() {
		return network;
	}
	
	public void setNetwork(double network) {
		this.network = network;
	}
	
	public double getDisk() {
		return disk;
	}

	public void setDisk(double disk) {
		this.disk = disk;
	}
	
	public double getCpuIdle() {
		return cpuIdle;
	}

	public void setCpuIdle(double cpuIdle) {
		this.cpuIdle = cpuIdle;
	}

	public double getCpuSteal() {
		return cpuSteal;
	}

	public void setCpuSteal(double cpuSteal) {
		this.cpuSteal = cpuSteal;
	}

	public double getCpuIOWait() {
		return cpuIOWait;
	}

	public void setCpuIOWait(double cpuIOWait) {
		this.cpuIOWait = cpuIOWait;
	}

	public double getCpuSoftirq() {
		return cpuSoftirq;
	}

	public void setCpuSoftirq(double cpuSoftirq) {
		this.cpuSoftirq = cpuSoftirq;
	}

	public double getNetworkEth0() {
		return networkEth0;
	}

	public void setNetworkEth0(double networkEth0) {
		this.networkEth0 = networkEth0;
	}
	
	@JsonIgnore
	public void setValue(String type, double value, String dimension) {	
		if (type.equalsIgnoreCase("CPU")) {
			if (dimension != null) {
				if (dimension.equals("idle")) {
					setCpuIdle(value);
				} else if (dimension.equals("iowait")) {
					setCpuIOWait(value);
				} else if (dimension.equals("stolen")) {
					setCpuSteal(value);
				} else if (dimension.equals("combined")) {
					setCpu(value);
				} else if (dimension.equals("softirq")) {
					setCpuSoftirq(value);
				}					
			} else {
				setCpu(value);
			}
		} else if (type.equalsIgnoreCase("Memory")) {
			setMemory(value);
		} else if (type.equalsIgnoreCase("Network")) {			
			if (dimension != null && dimension.equals("iface=eth0")) {
				setNetworkEth0(value);
			} else {
				setNetwork(value);
			}
		} else if (type.equalsIgnoreCase("Disks")) {
			setDisk(value);		
		}
	}
}
