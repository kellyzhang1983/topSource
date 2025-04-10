package com.zkcompany.controller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping(
            value = "/errorback",
            method = {RequestMethod.GET,RequestMethod.POST}
    )
    public Result errorback(Exception e) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"CircuitBreaker：服务器开小差了！，请稍后再试.....",e.getMessage());
    }

}
