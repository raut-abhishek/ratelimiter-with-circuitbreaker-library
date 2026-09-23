package com.demo;

public interface RateLimiter {
	boolean tryConsume();
}
