package com.zkcompany.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.RocketMQInfo;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.Order;
import com.zkcompany.rocketMQ.SendMessageMQ;
import com.zkcompany.service.SendMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendMessageServiceImpl implements SendMessageService {

    @Autowired
    private SendMessageMQ sendMessageMQ;

    @Override
    public Result orderSendMessage(Order order) throws Exception {
        JSONObject josn_order = (JSONObject)JSONObject.toJSON(order);
        sendMessageMQ.SendMessage_async(order, RocketMQInfo.rocketMQ_topic_orderProcess,josn_order);
        return new Result(true, StatusCode.SC_OK,"订单发送到MQ成功！",josn_order);
    }
}
