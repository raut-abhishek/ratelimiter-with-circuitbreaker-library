package com.demo;

public class CircuitBreaker {

	public enum State {
		CLOSED,
		OPEN,
		HALF_OPEN
	}

	private State currentState = State.CLOSED;
	private int failureCount = 0;
	private final int threshold;
	private long openedAt;
	private final long cooldownTime;

	public CircuitBreaker(int threshold, long cooldownTime) {
		this.threshold = threshold;
		this.cooldownTime = cooldownTime;
	}

	public synchronized void recordFailure() {
		failureCount += 1;
		if (failureCount >= threshold) {
			currentState = State.OPEN;
			openedAt = System.currentTimeMillis();
		}
	}

	public synchronized void recordSuccess() {
		failureCount = 0;
		if (currentState == State.HALF_OPEN)
			currentState = State.CLOSED;

	}

	public synchronized boolean allowRequest() {
		switch (currentState) {
			case CLOSED:
				return true;
			case HALF_OPEN:
				return true;
			case OPEN:
				if (System.currentTimeMillis() - openedAt >= cooldownTime) {
					currentState = State.HALF_OPEN;
					return true;
				}
				return false;

			default:
				return false;
		}

	}

}
