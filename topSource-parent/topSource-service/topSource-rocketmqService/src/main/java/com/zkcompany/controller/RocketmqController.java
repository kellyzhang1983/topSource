package com.zkcompany.controller;

import com.zkcompany.annotation.Inner;
import com.zkcompany.entity.Result;
import com.zkcompany.pojo.Order;
import com.zkcompany.service.SendMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rocketmq")
public class RocketmqController {

    @Autowired
    private SendMessageService sendMessageService;

    @Inner
    @RequestMapping("/orderSendMessage")
    public Result orderSendMessage(@RequestBody Order order){
        Result result = null;
        try {
            result = sendMessageService.orderSendMessage(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
