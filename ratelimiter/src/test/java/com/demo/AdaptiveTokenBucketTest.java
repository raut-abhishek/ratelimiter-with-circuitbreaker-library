package com.demo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class AdaptiveTokenBucketTest {

	@Test
	public void testNormalLoadBehavior() throws InterruptedException {
		AdaptiveTokenBucket bucket = new AdaptiveTokenBucket(5, 1);

		assertTrue(bucket.tryConsume());

		Thread.sleep(000);

		assertTrue(bucket.tryConsume());
	}

}
