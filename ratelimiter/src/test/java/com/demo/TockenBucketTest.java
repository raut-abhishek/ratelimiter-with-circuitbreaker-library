package com.demo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class TockenBucketTest {

	@Test
	public void testTokenBucketBehavior() throws InterruptedException {
		TockenBucket bucket = new TockenBucket(5, 1);
		for (int i = 0; i < 5; i++) {
			assertTrue(bucket.tryConsume());
		}

		assertFalse(bucket.tryConsume());

		Thread.sleep(1000);

		assertTrue(bucket.tryConsume());

	}
}
