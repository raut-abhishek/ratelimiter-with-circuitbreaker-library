package com.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import com.demo.core.TockenBucket;

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
