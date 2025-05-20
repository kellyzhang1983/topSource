package com.zkcompany.contorller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class FallbackController {

    @RequestMapping(
            value = "/errorback",
            method = {RequestMethod.GET,RequestMethod.POST}
    )
    public Result errorback(Exception e) {
        log.error("market_gateway_CircuitBreaker：market-server服务器开小差了！，请稍后再试.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"market_gateway_CircuitBreaker：market-server服务器开小差了！，请稍后再试.....",e.getMessage());
    }

}
