package com.demo.core;

public class TokenBucket implements RateLimiter{
	private final int capasity;
	private final double refillRatePerSecond;
	private double tokens;
	private long lastRefillTimeStamp;

	public TokenBucket(int capasity, double refillRatePerSecond) {
		this.capasity = capasity;
		this.refillRatePerSecond = refillRatePerSecond;
		this.tokens = capasity;
		this.lastRefillTimeStamp = System.currentTimeMillis();
	}

	public synchronized boolean tryConsume() {
		refill();
		if (tokens >= 1) {
			tokens -= 1;
			return true;
		} else {
			return false;
		}
	}

	private void refill() {
		long now = System.currentTimeMillis();
		double passedSeconds = (now - lastRefillTimeStamp) / 1000;
		double tokensToAdd = passedSeconds * refillRatePerSecond;
		tokens = Math.min(capasity, tokens + tokensToAdd);
		lastRefillTimeStamp = now;

	}
}
