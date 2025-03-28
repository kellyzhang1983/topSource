package com.zkcompany.contorller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/errorback")
    public Result errorback(Exception e) {
        return new Result<>(false, StatusCode.REMOTEERROR,"user_gateway_CircuitBreaker：user-server服务器开小差了！，请稍后再试.....",e.getMessage());
    }

}
