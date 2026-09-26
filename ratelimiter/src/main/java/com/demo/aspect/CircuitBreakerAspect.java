package com.demo.aspect;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.demo.annotation.CircuitBreakerProtected;
import com.demo.core.CircuitBreaker;

@Aspect
@Component
public class CircuitBreakerAspect {
	
	private final ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();
	
	@Around("@annotation(com.demo.annotation.CircuitBreakerProtected)")
	public Object checkCircuitBreaker(ProceedingJoinPoint joinPoint)throws Throwable {
		
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		CircuitBreakerProtected annotation = method.getAnnotation(CircuitBreakerProtected.class);
		
		String key = method.getName();
		
		CircuitBreaker breaker = breakers.computeIfAbsent(key, k -> new CircuitBreaker(annotation.threshold(), annotation.cooldownTime()));
		
		if(!breaker.allowRequest()) {
			throw new CircuitBreakerOpenException("Circuit breaker open for: " + key);
		}
		
		try {
			Object result = joinPoint.proceed();
			breaker.recordSuccess();
			return result;
		}catch (Throwable ex){
			breaker.recordFailure();
			throw ex;
		}
		
	}
	
}
