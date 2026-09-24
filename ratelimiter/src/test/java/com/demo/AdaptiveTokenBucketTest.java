package com.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import com.demo.core.AdaptiveTokenBucket;

public class AdaptiveTokenBucketTest {

	@Test
	public void testNormalLoadBehavior() throws InterruptedException {
		AdaptiveTokenBucket bucket = new AdaptiveTokenBucket(5, 1);

		assertTrue(bucket.tryConsume());

		Thread.sleep(000);

		assertTrue(bucket.tryConsume());
	}

}
