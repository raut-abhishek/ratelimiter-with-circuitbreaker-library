package com.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demo.annotation.RateLimited;
import com.demo.aspect.RateLimitExceedException;

@RestController
public class DemoController {
	
	@RateLimited(capacity=3, refillRate = 1)
	@GetMapping("/home")
	public String home() {
		return "Request allowed!";
	}
	
	
	
	@ExceptionHandler(RateLimitExceedException.class)
	@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
	public String handleRateLimitException(RateLimitExceedException ex) {
		return ex.getMessage();
	}
}
