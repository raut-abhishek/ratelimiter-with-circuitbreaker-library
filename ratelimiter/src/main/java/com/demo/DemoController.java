package com.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demo.annotation.CircuitBreakerProtected;
import com.demo.annotation.RateLimited;
import com.demo.aspect.CircuitBreakerOpenException;
import com.demo.aspect.ErrorResponse;
import com.demo.aspect.RateLimitExceededException;
import com.demo.core.RedisTokenBucket;

@RestController
public class DemoController {
	
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	private RedisTokenBucket redisBucket;
	@GetMapping("/redis-test")
	public String redisTest() {
		if(redisBucket == null) {
			redisBucket = new RedisTokenBucket("redistest", 1,1, redisTemplate);
		}
		if(redisBucket.tryConsume()) {
			return "Allowed!!!";
		}
		else {
			return "Rejected - rate limit exceeded!!";
		}
	}
	
	
	
	
	

	@RateLimited(capacity = 3, refillRate = 1)
	@GetMapping("/home")
	public String home() {
		return "Request allowed!";
	}

	@CircuitBreakerProtected(threshold = 5, cooldownTime = 5000)
	@GetMapping("/test")
	public String test(@RequestParam(defaultValue = "false") boolean fail) {
		if (fail) {
			throw new RuntimeException("Simulated Failure!");
		}
		return "Success";
	}
	
	@RateLimited(capacity = 2, refillRate = 1, key = "#userId")
	@GetMapping("/greet")
	public String greet(@RequestParam String userId) {
		return "Hello " + userId + "!";
	}

	@GetMapping("/")
	public String m1() {
		return "Hey ";
	}
	

	@ExceptionHandler(RateLimitExceededException.class)
	@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
	public ErrorResponse handleRateLimitException(RateLimitExceededException ex) {
		return new ErrorResponse(429, "Too Many Requests", ex.getMessage());
	}

	@ExceptionHandler(CircuitBreakerOpenException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ErrorResponse handleCircuitBreakerOpen(CircuitBreakerOpenException ex) {
		return new ErrorResponse(503,"Service Unavailable", ex.getMessage());
	}
}
