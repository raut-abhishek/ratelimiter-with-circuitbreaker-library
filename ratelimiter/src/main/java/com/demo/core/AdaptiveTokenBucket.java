package com.demo.core;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class AdaptiveTokenBucket implements RateLimiter{
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
		OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
		return osBean.getCpuLoad();
	}

	private double getEffectiveCapacity() {
		double load = getCpuLoad();
		if (load >= cpuLoadHigh) {
			return baseCapasity * 0.2;
		} else if (load >= cpuLoadMed) {
			return baseCapasity * 0.5;
		} else {
			return baseCapasity;
		}

	}

	private void refill() {
		long now = System.currentTimeMillis();
		double elapsedSeconds = (now - lastRefillTimeStamp) / 1000.0;
		double tokensToAdd = elapsedSeconds * getEffectiveRefillRate();
		tokens = Math.min(getEffectiveCapacity(), tokens + tokensToAdd);
		lastRefillTimeStamp = now;
	}

	private double getEffectiveRefillRate() {
		double load = getCpuLoad();

		if (load >= cpuLoadHigh) {
			return baseRefillRate * 0.2;
		} else if (load >= cpuLoadMed) {
			return baseRefillRate * 0.5;
		} else {
			return baseRefillRate;
		}
	}
	
	
	public synchronized boolean tryConsume() {
		refill();
		if(tokens >=1) {
			tokens -= 1;
			return true;	
		}else {
			return false;
		}
	}

}
