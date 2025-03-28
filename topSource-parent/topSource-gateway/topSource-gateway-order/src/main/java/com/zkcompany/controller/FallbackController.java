package com.zkcompany.controller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/errorback")
    public Result errorback(Exception e) {
        return new Result<>(false, StatusCode.REMOTEERROR,"CircuitBreaker：服务器开小差了！，请稍后再试.....",e.getMessage());
    }

}
