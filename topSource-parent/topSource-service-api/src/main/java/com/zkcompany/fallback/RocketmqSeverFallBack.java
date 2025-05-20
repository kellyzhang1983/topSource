package com.zkcompany.fallback;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.fegin.RocketmqCenterFegin;
import com.zkcompany.pojo.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RocketmqSeverFallBack implements RocketmqCenterFegin {
    @Override
    public Result orderSendMessage(Order order) {
        log.error("RocketmqCenterFegin（rocketmq-server）：远程服务调用orderSendMessage失败,请查看详细信息！.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"RocketmqCenterFegin（rocketmq-server）：远程服务调用orderSendMessage失败,请查看详细信息！.....");
    }
}
