package com.demo.aspect;

public class RateLimitExceededException extends RuntimeException{
	public RateLimitExceededException(String message) {
		super(message);
	}
}
