package com.demo;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class AdaptiveTokenBucket {
	private final double cpuLoadMed = 0.5;
	private final double cpuLoadHigh = 0.8;
	private final int baseCapasity;
	private final int baseRefillRate;
	private double tokens;
	private long lastRefillTimeStamp;

	public AdaptiveTokenBucket(int baseCapasity, int baseRefillRate) {
		this.baseCapasity = baseCapasity;
		this.baseRefillRate = baseRefillRate;
		lastRefillTimeStamp = System.currentTimeMillis();
		this.tokens = baseCapasity;
	}

	private double getCpuLoad() {
		com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
		return osBean.getCpuLoad();
	}

	public double getEffectiveCapacity() {
		double load = getCpuLoad();
		switch (load) {
			case load > cpuLoadHigh:
				return (baseCapasity / 100) * 20;
			case load > cpuLoadMed:
				return (baseCapasity / 100) * 50;
			case load < cpuLoadMed:
				return (baseCapasity / 100) * 100;
			default:
				return (baseCapasity / 100) * 100;
		}

	}

}
