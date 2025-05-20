package com.zkcompany.rocketMQ;

import com.alibaba.fastjson.JSONObject;
import com.zkcompany.entity.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SendMessageMQ {
    //Message的子类，Message的扩展参数
    //MessageExt messageExt = new MessageExt();
    //直接设置需要延迟的时间，例如300秒，300毫秒
    //messageExt.setDelayTimeSec(300);
    //解决消息幂等性问题，在MSGID设置一个唯一值
    //messageExt.setMsgId(order.getId());

    //这种方式也是同步发送，但是没有反馈值判断发送状态，只有通过捕获异常来判断是否发送成功
    //rocketMQTemplate.convertAndSend(RocketMQInfo.rocketMQ_topic_orderProcess,order);
    //同步发送消息,性能较慢；但是可以保证数据的安全性；
    //sendResult = rocketMQTemplate.syncSend(RocketMQInfo.rocketMQ_topic_orderProcess, MessageBuilder.withPayload(order).build());
    //异步发送消息,性能较快；可以保证数据的安全性，但producter这个线程要一直监听；

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void SendMessage_async_delay(Object obj, String topic,long timeout, int delaylevel){
        //messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        Message<Object> message = MessageBuilder.withPayload(obj).build();
        //rocketMQTemplate.syncSend(RocketMQInfo.rocketMQ_topic_orderProcessDelay, MessageBuilder.withPayload(order).build(), 3000, 9);
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送状态："+ sendResult.getSendStatus());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("错误码：" + StatusCode.REMOTEERROR + "；message：RocketMQ发送消息失败！请稍后再试....." + ";详细信息：" + throwable.getMessage());
                throw new RuntimeException(throwable);
            }
        },timeout,delaylevel);
    }

    public void SendMessage_async_delay(Object obj, String topic,long timeout, int delaylevel, JSONObject jsonObject){
        //messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        Message message = MessageBuilder.withPayload(obj).build();
        //rocketMQTemplate.syncSend(RocketMQInfo.rocketMQ_topic_orderProcessDelay, MessageBuilder.withPayload(order).build(), 3000, 9);
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送状态："+ sendResult.getSendStatus() + "; 发送信息：" + jsonObject);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("错误码：" + StatusCode.REMOTEERROR + "；message：RocketMQ发送消息失败！请稍后再试....."
                        + ";详细信息：" + jsonObject + "；错误异常：" + throwable.getMessage() );
                throw new RuntimeException(throwable);
            }
        },timeout,delaylevel);
    }

    public void SendMessage_async(Object obj, String topic, JSONObject jsonObject){
        //messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        Message<Object> message = MessageBuilder.withPayload(obj).build();
        //rocketMQTemplate.syncSend(RocketMQInfo.rocketMQ_topic_orderProcessDelay, MessageBuilder.withPayload(order).build(), 3000, 9);
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送状态："+ sendResult.getSendStatus() + "; 发送信息：" + jsonObject);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("错误码：" + StatusCode.REMOTEERROR + "；message：RocketMQ发送消息失败！请稍后再试....."
                        + ";详细信息：" + jsonObject + "；错误异常：" + throwable.getMessage() );
                throw new RuntimeException(throwable);
            }
        });
    }


    public void SendMessage_async(Object obj, String topic){
        //messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        Message<Object> message = MessageBuilder.withPayload(obj).build();
        //rocketMQTemplate.syncSend(RocketMQInfo.rocketMQ_topic_orderProcessDelay, MessageBuilder.withPayload(order).build(), 3000, 9);
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送状态："+ sendResult.getSendStatus() + ";");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("错误码：" + StatusCode.REMOTEERROR
                        + ";错误异常：" + throwable.getMessage());
                throw new RuntimeException(throwable);
            }
        });
    }
}
