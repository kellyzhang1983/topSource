package com.zkcompany.contorller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class FallbackController {

    @RequestMapping(
            value = "/errorback",
            method = {RequestMethod.GET,RequestMethod.POST}
    )
    public Result errorback(Exception e) {
        log.error("search_gateway_CircuitBreaker：search-server服务器开小差了！，请稍后再试.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"search_gateway_CircuitBreaker：search-server服务器开小差了！，请稍后再试.....",e.getMessage());
    }

}
