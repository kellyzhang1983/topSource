package com.zkcompany.contorller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import org.springframework.web.bind.annotation.*;

@RestController
public class FallbackController {

    @RequestMapping(
            value = "/errorback",
            method = {RequestMethod.GET,RequestMethod.POST}
    )
    public Result errorback(Exception e) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"search_gateway_CircuitBreaker：search-server服务器开小差了！，请稍后再试.....",e.getMessage());
    }

}
