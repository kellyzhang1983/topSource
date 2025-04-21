package com.zkcompany.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.dao.UserOrderMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Order;
import com.zkcompany.service.ProcessOrderData;
import com.zkcompany.uitl.ConvertObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SynOrderData implements ProcessOrderData {

    @Autowired
    private UserOrderMapper userOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Integer order_addOrUpdateRedis(List<CanalEntry.Column> columns, Map<String,String> userMap,Integer count) {
        Order order = new Order();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            switch (name){
                case "user_id":
                    ConvertObject.convertOrder(order,name,column.getValue());
                    String userid = userMap.get(column.getValue());
                    if(StringUtils.isEmpty(userid)){
                        //查询数据库，把所有user_id想同的数据全部查询出来，返回一个List<Order>
                        List<Order> orders = userOrderMapper.searchUserOrder(name);
                        try {
                            //放入Redis中，数据格式为Map<user_id,List<Order>>
                            redisTemplate.boundHashOps(SystemConstants.redis_userOrder).put(column.getValue(),orders);
                            //记录该用户已从数据库通过userid查询出已修改所有的订单，当再查询的时候不予执行；
                            userMap.put(column.getValue(),"1");
                            count = count + 1 ;
                        } catch (Exception e) {
                            // 处理异常，例如记录日志或采取其他措施
                            log.error("Redis operation order_addOrUpdateRedis failed: " + e.getMessage());
                        }
                    }

                    break;
                default:
                    ConvertObject.convertOrder(order,name,column.getValue());
                    break;
            }
        }
        try {
            //放入Redis中，数据格式为Map<order_id,List<Order>>
            redisTemplate.boundHashOps(SystemConstants.redis_Order).put(order.getId(),order);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("Redis operation order_addOrUpdateRedis failed: " + e.getMessage());
        }
        return count;

    }

    @Override
    public void order_deleteRedis(List<CanalEntry.Column> columns) {
        String id = "";

        for (CanalEntry.Column column : columns){
            if(column.getName().equals("id")){
                id = column.getValue();
                break;
            }
        }
        try {
            redisTemplate.boundHashOps(SystemConstants.redis_Order).delete(id);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("Redis operation order_deleteRedis failed: " + e.getMessage());
        }
    }
}
