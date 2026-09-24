package com.demo.core;

public interface RateLimiter {
	boolean tryConsume();
}
