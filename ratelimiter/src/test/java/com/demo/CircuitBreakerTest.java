package com.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class CircuitBreakerTest {

	@Test
	public void testCircuitBreakerCycle() throws InterruptedException {
		CircuitBreaker breaker = new CircuitBreaker(3, 1000);

		assertTrue(breaker.allowRequest());
		assertTrue(breaker.allowRequest());
		assertTrue(breaker.allowRequest());

		breaker.recordFailure();
		breaker.recordFailure();
		breaker.recordFailure();

		assertFalse(breaker.allowRequest());

		Thread.sleep(1100);

		assertTrue(breaker.allowRequest());
		breaker.recordSuccess();
		assertTrue(breaker.allowRequest());

	}
}
