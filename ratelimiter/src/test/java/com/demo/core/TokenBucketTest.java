package com.demo.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import com.demo.core.TokenBucket;

public class TokenBucketTest {

	@Test
	public void testTokenBucketBehavior() throws InterruptedException {
		TokenBucket bucket = new TokenBucket(5, 1);
		for (int i = 0; i < 5; i++) {
			assertTrue(bucket.tryConsume());
		}

		assertFalse(bucket.tryConsume());

		Thread.sleep(1000);

		assertTrue(bucket.tryConsume());

	}
}
