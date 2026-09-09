package com.demo;

import java.util.LinkedList;
import java.util.Queue;


public class SlidingWindowLimiter {
	private final int maxRequests;
	private final long wondowSizeInMillis;
	private final Queue<Long> timeStamps;
	
	
	public SlidingWindowLimiter(int maxRequests, long windowSizeInMillis) {
		this.maxRequests = maxRequests;
		this.wondowSizeInMillis = windowSizeInMillis;
		this.timeStamps = new LinkedList<>();
	}
	
	
	public synchronized boolean tryConsume() {
		long now = System.currentTimeMillis();
		long windowStart = now - wondowSizeInMillis;
		
		
		while(!timeStamps.isEmpty() && timeStamps.peek() < windowStart) {
			timeStamps.poll();
		}
		
		if(timeStamps.size() < maxRequests) {
			timeStamps.add(now);
			return true;
		}else {
			return false;			
		}
		
	}
	
}
