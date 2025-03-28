package com.zkcompany.config;

import com.zkcompany.entity.RocketMQInfo;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.List;

public class RocketMQCustomProperty implements AllocateMessageQueueStrategy {

    private final List<MessageQueue> specifiedQueues = new ArrayList<>();

    // 假设手动指定消费队列0、1、2
    {
        specifiedQueues.add(new MessageQueue(RocketMQInfo.rocketMQ_topic_orderProcess, "broker-a", 0));
        //specifiedQueues.add(new MessageQueue(RocketMQInfo.rocketMQ_topic_orderProcess, "broker-a", 1));
        //specifiedQueues.add(new MessageQueue(RocketMQInfo.rocketMQ_topic_orderProcess, "broker-a", 2));
    }

    @Override
    public List<MessageQueue> allocate(String s, String s1, List<MessageQueue> list, List<String> list1) {
        List<MessageQueue> result = new ArrayList<>();
        for (MessageQueue mq : list) {
            if (specifiedQueues.contains(mq)) {
                result.add(mq);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "RocketMQCustomProperty";
    }
}
