package com.demo.aspect;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.demo.annotation.RateLimited;
import com.demo.core.RateLimiter;
import com.demo.core.TokenBucket;

@Aspect
@Component
public class RateLimitedAspect {
	private final ConcurrentHashMap<String, RateLimiter> buckets = new ConcurrentHashMap<>();
	private final ExpressionParser parser = new SpelExpressionParser();

	@Around("@annotation(com.demo.annotation.RateLimited)")
	public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		RateLimited annotation = method.getAnnotation(RateLimited.class);

		String key = method.getName();
		if (!annotation.key().isEmpty()) {
			String resolvedKey = resolveKey(annotation.key(), signature, joinPoint.getArgs());
			key = key + " : " + resolvedKey;
		}

		RateLimiter bucket = buckets.computeIfAbsent(key,
				k -> new TokenBucket(annotation.capacity(), annotation.refillRate()));

		if (bucket.tryConsume()) {
			return joinPoint.proceed();
		} else {
			throw new RateLimitExceedException("Rate limit excedded for " + key);
		}

	}

	private String resolveKey(String expression, MethodSignature signature, Object[] args) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		String[] paramNames = signature.getParameterNames();
		for (int i = 0; i < paramNames.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}

		Expression exp = parser.parseExpression(expression);
		Object value = exp.getValue(context);
		return String.valueOf(value);
	}

}
