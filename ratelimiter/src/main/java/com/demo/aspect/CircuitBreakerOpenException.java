package com.demo.aspect;

public class CircuitBreakerOpenException extends RuntimeException{
	public CircuitBreakerOpenException(String message) {
		super(message);
	}
}
