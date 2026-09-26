package com.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demo.annotation.CircuitBreakerProtected;
import com.demo.annotation.RateLimited;
import com.demo.aspect.CircuitBreakerOpenException;
import com.demo.aspect.RateLimitExceedException;

@RestController
public class DemoController {

	@RateLimited(capacity = 3, refillRate = 1)
	@GetMapping("/home")
	public String home() {
		return "Request allowed!";
	}

	@CircuitBreakerProtected(threshold = 5, cooldownTime = 5000)
	@GetMapping("/risky")
	public String risky(@RequestParam(defaultValue = "false") boolean fail) {
		if (fail) {
			throw new RuntimeException("Simulated Failure!");
		}
		return "Success";
	}

	@ExceptionHandler(RateLimitExceedException.class)
	@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
	public String handleRateLimitException(RateLimitExceedException ex) {
		return ex.getMessage();
	}

	@ExceptionHandler(CircuitBreakerOpenException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public String handleCircuitBreakerOpen(CircuitBreakerOpenException ex) {
		return ex.getMessage();
	}
}
