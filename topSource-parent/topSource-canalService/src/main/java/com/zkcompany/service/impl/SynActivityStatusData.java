package com.zkcompany.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.dao.ActivityStatusMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.ActivityStatus;
import com.zkcompany.service.ProcessActivityStatusData;
import com.zkcompany.uitl.ConvertObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Slf4j
public class SynActivityStatusData implements ProcessActivityStatusData {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ActivityStatusMapper activityStatusMapper;

    @Override
    public void activityStatus_addOrUpdateRedis(List<CanalEntry.Column> columns) {
        ActivityStatus activityStatus = new ActivityStatus();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            switch (name){
                case "user_id":
                    List<ActivityStatus> activityStatusList = activityStatusMapper.selectActivityStatus(column.getValue());
                    try {
                        ConvertObject.convertActivityStatus(activityStatus, name, column.getValue());
                        redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).put(activityStatus.getUserId(),activityStatusList);
                    } catch (Exception e) {
                        // 处理异常，例如记录日志或采取其他措施
                        log.error("Redis operation activityStatus_addOrUpdateRedis  failed: " + e.getMessage());
                    }
                default:
                    ConvertObject.convertActivityStatus(activityStatus, name, column.getValue());
            }
        }

        try {
            redisTemplate.boundHashOps(SystemConstants.redis_activityStatus).put(activityStatus.getOrderId(),activityStatus);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("Redis operation activityStatus_addOrUpdateRedis  failed: " + e.getMessage());
        }
    }

    @Override
    public void activityStatus_deleteRedis(List<CanalEntry.Column> columns) {
        String orderId = "";
        String userId = "";

        for (CanalEntry.Column column : columns){
            if(column.getName().equals("order_id")){
                orderId = column.getValue();
            }

            if(column.getName().equals("user_id")){
                userId = column.getValue();
            }

            if(!StringUtils.isEmpty(orderId)&& !StringUtils.isEmpty(userId)){
                break;
            }
        }
        List<ActivityStatus> activityStatusList = (List<ActivityStatus>)redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).get(userId);
        for(ActivityStatus activityStatus : activityStatusList){
            if(orderId.equals(activityStatus.getOrderId())){
                activityStatusList.remove(activityStatus);
                try {
                    redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).put(activityStatus.getUserId(),activityStatusList);
                } catch (Exception e) {
                    // 处理异常，例如记录日志或采取其他措施
                    log.error("Redis operation activityStatus_deleteRedis  failed: " + e.getMessage());
                }
                break;
            }
        }

        try {
            redisTemplate.boundHashOps(SystemConstants.redis_activityStatus).delete(orderId);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("Redis operation activityStatus_deleteRedis failed: " + e.getMessage());
        }

    }
}
