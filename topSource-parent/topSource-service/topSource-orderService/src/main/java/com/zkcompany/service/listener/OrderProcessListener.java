package com.zkcompany.service.listener;

import com.zkcompany.entity.RocketMQInfo;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.User;
import com.zkcompany.service.OrderService;
import com.zkcompany.task.OrderCreateSendMQ;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RocketMQMessageListener(topic = RocketMQInfo.rocketMQ_topic_orderProcess,
        consumerGroup = RocketMQInfo.rocketMQ_consumer_orderProcess,
        consumeThreadNumber = 10,
        consumeThreadMax = 20)
@Slf4j
public class OrderProcessListener implements RocketMQListener<Order>{
/*
    {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setAllocateMessageQueueStrategy(new RocketMQCustomProperty());
    }
*/

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderService orderService;
    @Override
    public void onMessage(Order message) {
        try {

            log.info("===========================================监听数据:<" + message.getId() + ">===============================================");
            requestAuth(message.getUser_id());
            orderService.cretaOrder(message);
            log.info("===========================================数据处理完成：<" + message.getId() + ">===============================================");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void requestAuth(String userid){
        String token = OrderCreateSendMQ.token;
        HttpServletRequest request = new com.zkcompany.entity.HttpServletRequest();
        HttpServletResponse response = new com.zkcompany.entity.HttpServletResponse();
        if(StringUtils.isEmpty(token)){
            User user = (User)redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(userid);
            token = (String) redisTemplate.boundHashOps(SystemConstants.redis_userToken).get(user.getUsername());
            if(StringUtils.isEmpty(token)){
                throw new RuntimeException(user.getUsername() + "用户已经注销，请重新登录!");
            }
        }
        // 手动创建 HttpServletRequest 和 HttpServletResponse 模拟对象
        ((com.zkcompany.entity.HttpServletRequest) request).addHeader("Authorization",token);
        ServletRequestAttributes attributes = new ServletRequestAttributes(request, response);
        // 将请求上下文设置到 RequestContextHolder 中
        RequestContextHolder.setRequestAttributes(attributes);
    }
}
