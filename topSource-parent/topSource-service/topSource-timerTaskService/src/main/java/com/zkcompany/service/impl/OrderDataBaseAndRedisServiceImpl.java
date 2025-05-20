package com.zkcompany.service.impl;

import com.zkcompany.dao.OrderGoodsMapper;
import com.zkcompany.dao.UserOrderMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.OrderGoods;
import com.zkcompany.service.OrderDataBaseAndRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class OrderDataBaseAndRedisServiceImpl implements OrderDataBaseAndRedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserOrderMapper userOrderMapper;

    @Autowired
    private OrderGoodsMapper orderGoodsMapper;
    @Override
    public boolean checkTbOrder() {
        log.info(".........检查db_order库（tb_order表）开始.........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_Order).keys();
        List<Order> orderList = userOrderMapper.selectAll();
        boolean sync = true;
        if(keys.size() != orderList.size()){
            log.info("开始同步到Redis中！<<<<<<<<<");
            redisTemplate.delete(SystemConstants.redis_Order);
            int count = 0;
            for(Order order : orderList){
                redisTemplate.boundHashOps(SystemConstants.redis_Order).put(order.getId(),order);
                count ++;
            }
            sync = false;
            log.info("数据同步到Redis（redis_Order），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_order库（tb_order表）结束.........");
        return sync;
    }

    @Override
    public boolean checkTbUserOrder() {
        log.info(".........检查db_order库（tb_order表）开始........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_userOrder).keys();
        List<Order> orderList = userOrderMapper.selectAllUserOrder();
        boolean sync = true;
        int count = 0;
        if(keys.size() != orderList.size()){
            log.info("开始删除Redis中的redis_userOrder数据<<<<<<<<<");
            sync = false;
            for (Object key : keys) {
                Long delete = redisTemplate.boundHashOps(SystemConstants.redis_userOrder).delete(key);
                count = count + Integer.valueOf(delete.toString());
            }
            log.info("总共删除Redis中的redis_userOrder数据：" + count + "条数据！<<<<<<<<<");
        }
        count = 0;
        for(Order order : orderList){
            List<Order> userOrder_mysql = userOrderMapper.searchUserOrder(order.getUserId());
            List<Order> userOrder_redis = (List<Order>)redisTemplate.boundHashOps(SystemConstants.redis_userOrder).get(order.getUserId());
            if(userOrder_mysql.size() != (userOrder_redis == null ? 0 : userOrder_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中！<<<<<<<<<");
                    sync = false;
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userOrder).put(order.getUserId(), userOrder_mysql);
                count ++;

            }

        }

        if(count != 0) {
            log.info("数据同步到Redis（redis_userOrder），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_order库（tb_order表）结束.........");
        return sync;
    }

    @Override
    public boolean checkTbOrderGoods() {
        log.info(".........检查db_order库（tb_order_goods表）开始.........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).keys();
        List<OrderGoods> orderGoodsList = orderGoodsMapper.selectOrder();
        boolean sync = true;
        if(keys.size() != orderGoodsList.size()){
            log.info("开始同步到Redis中！<<<<<<<<<");
            redisTemplate.delete(SystemConstants.redis_orderGoods);
            int count = 0;
            for(OrderGoods OrderGoods : orderGoodsList){
                List<OrderGoods> orderGoods = orderGoodsMapper.selectOrderGoods(OrderGoods.getOrderId());
                redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).put(OrderGoods.getOrderId(),orderGoods);
                count ++;
            }
            sync = false;
            log.info("数据同步到Redis（redis_orderGoods），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_order库（tb_order_goods表）结束.........");
        return sync;
    }
}
