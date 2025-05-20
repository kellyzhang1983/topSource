package com.zkcompany.listener;

import com.zkcompany.entity.RocketMQInfo;
import com.zkcompany.pojo.Order;
import com.zkcompany.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@RocketMQMessageListener(topic = RocketMQInfo.rocketMQ_topic_orderProcessDelay,
        consumerGroup = RocketMQInfo.rocketMQ_consumer_orderProcess_delay,
        consumeThreadNumber = 5,
        consumeThreadMax = 10,
        maxReconsumeTimes = 3,
        suspendCurrentQueueTimeMillis = 5000)
@Slf4j
public class OrderProcesseDelayListener implements RocketMQListener<Order> {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderService orderService;
    @Override
    public void onMessage(Order message) {
        String redisKey = "consumerGroup:" + RocketMQInfo.rocketMQ_consumer_orderProcess_delay + "userId:" + message.getUserId() +   "msg:" + message.getId() ;
        try {
            if(redisTemplate.opsForValue().setIfAbsent(redisKey, "1", 24, TimeUnit.HOURS)){
                log.info("===========================================监听延时（delay）数据:<" + message.getId() + ">===============================================");
                orderService.cancelMarketOrder(message);
                log.info("===========================================延时（delay）数据处理完成：<" + message.getId() + ">===============================================");
            }else {
                log.warn("消息重复：{}", message.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*private void requestAuth(String userid){
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
    }*/
}
