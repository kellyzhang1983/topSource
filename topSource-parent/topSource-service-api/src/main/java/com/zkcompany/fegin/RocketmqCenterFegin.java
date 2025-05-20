package com.zkcompany.fegin;

import com.zkcompany.annotation.InnerMethodCall;
import com.zkcompany.config.FeignApiConfig;
import com.zkcompany.entity.Result;
import com.zkcompany.fallback.RocketmqSeverFallBack;
import com.zkcompany.pojo.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "rocket-server",configuration = FeignApiConfig.class, fallback = RocketmqSeverFallBack.class)
public interface RocketmqCenterFegin {

    @RequestMapping("/rocketmq/orderSendMessage")
    @InnerMethodCall
    public Result orderSendMessage(@RequestBody Order order);
}
