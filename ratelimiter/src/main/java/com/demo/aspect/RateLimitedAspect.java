package com.demo.aspect;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.demo.annotation.RateLimited;
import com.demo.core.RateLimiter;
import com.demo.core.TokenBucket;

@Aspect
@Component
public class RateLimitedAspect {
	private final ConcurrentHashMap<String, RateLimiter> buckets = new ConcurrentHashMap<>();
	
	
	@Around("@annotation(com.demo.annotation.RateLimited)")
	public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable{
		
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		RateLimited annotation = method.getAnnotation(RateLimited.class);
		
		
		String key = method.getName();
		
		RateLimiter bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(annotation.capacity(), annotation.refillRate()));
		
		if(bucket.tryConsume()) {
			return joinPoint.proceed();
		}else {
			throw  new RuntimeException("Rate limit excedded for " + key);
		}
		
	}
	
}
