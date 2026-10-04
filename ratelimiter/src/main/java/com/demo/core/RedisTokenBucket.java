package com.demo.core;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisTokenBucket implements RateLimiter{
	
	private final String key;
	private final int capacity;
	private final double refillRate;
	private final RedisTemplate<String, String> redisTemplate;
	
	
	public RedisTokenBucket(String key, int capacity, double refillRate, RedisTemplate<String, String> redisTemplate) {
		this.key = key;
		this.capacity = capacity;
		this.refillRate = refillRate;
		this.redisTemplate = redisTemplate;
	}
	
	@Override
	public synchronized boolean tryConsume() {

		String tokensKey = key + ":tokens";
		String timestampKey = key + ":timestamp";
		
		String tokensStr = redisTemplate.opsForValue().get(tokensKey);
		String timestampStr = redisTemplate.opsForValue().get(timestampKey);
		
		double tokens = (tokensStr != null) ? Double.parseDouble(tokensStr) : capacity;
		long lastRefill = (timestampStr != null) ? Long.parseLong(timestampStr) : System.currentTimeMillis();
		
		long now = System.currentTimeMillis();
		double elapsedSeconds = (now - lastRefill)/1000.0;
		double tokensToAdd = elapsedSeconds * refillRate;
		tokens = Math.min(capacity, tokens + tokensToAdd);
		
		boolean allowed;
		if(tokens >= 1) {
			tokens -= 1;
			allowed = true;
		}else {
			allowed = false;
		}
		
		redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens));
		redisTemplate.opsForValue().set(timestampKey, String.valueOf(now));
		
		
		return allowed;
		
	}
	
}
