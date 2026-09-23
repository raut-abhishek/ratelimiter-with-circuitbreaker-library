package com.demo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class AdaptiveTokenBucketTest {

	@Test
	public void testNormalLoadBehavior() throws InterruptedException {
		AdaptiveTokenBucket bucket = new AdaptiveTokenBucket(5, 1);

		for (int i = 0; i < 5; i++) {
			assertTrue(bucket.tryConsume());
		}
		assertFalse(bucket.tryConsume());

		Thread.sleep(1000);
		assertTrue(bucket.tryConsume());
	}

}
