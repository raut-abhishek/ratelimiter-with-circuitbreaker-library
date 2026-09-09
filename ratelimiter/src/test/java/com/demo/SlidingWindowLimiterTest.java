package com.demo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;




public class SlidingWindowLimiterTest {
	
	
	@Test
	public void testSlidingWindow() throws InterruptedException{
		SlidingWindowLimiter limiter = new SlidingWindowLimiter(3, 2000);
		
		assertTrue(limiter.tryConsume());
		assertTrue(limiter.tryConsume());
		assertTrue(limiter.tryConsume());;
		
		assertFalse(limiter.tryConsume());
		
		Thread.sleep(2100);
		
		assertTrue(limiter.tryConsume());
	}
}
