package com.zkcompany.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.dao.OrderGoodsMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.OrderGoods;
import com.zkcompany.service.ProcessOrderGoodsData;
import com.zkcompany.uitl.ConvertObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SynOrderGoodsData implements ProcessOrderGoodsData {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderGoodsMapper orderGoodsMapper;

    @Override
    public Integer orderGoods_addOrUpdateRedis(List<CanalEntry.Column> columns, Map<String,String> userMap, Integer count) {
        OrderGoods orderGoods = new OrderGoods();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            switch (name){
                case "order_id":
                    ConvertObject.convertOrderGoods(orderGoods,name,column.getValue());
                    String orderId = userMap.get(column.getValue());
                    if(StringUtils.isEmpty(orderId)){
                        //查询数据库，把所有Order_id想同的数据全部查询出来，返回一个 List<OrderGoods>
                        List<OrderGoods> orderGoodsList = orderGoodsMapper.selectOrderGoods(column.getValue());
                        try {
                            //放入Redis中，数据格式为Map<order_id,List<OrderGoods>
                            redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).put(column.getValue(),orderGoodsList);
                            //记录该用户已从数据库通过order_id查询出已修改所有的商品信息，当再查询的时候不予执行；
                            userMap.put(column.getValue(),"1");
                            count = count + 1 ;
                        } catch (Exception e) {
                            // 处理异常，例如记录日志或采取其他措施
                            log.error("Redis operation orderGoods_addOrUpdateRedis failed: " + e.getMessage());
                        }
                    }
                    break;
                default:
                    ConvertObject.convertOrderGoods(orderGoods,name,column.getValue());
                    break;
            }
        }

        return count;
    }

    @Override
    public void orderGoods_deleteRedis(List<CanalEntry.Column> columns) {
        String id = "";
        String orderId = "";

        for (CanalEntry.Column column : columns){
            if(column.getName().equals("id")){
                id = column.getValue();
            }
            if(column.getName().equals("order_id")){
                orderId = column.getValue();
            }
            if(!StringUtils.isEmpty(id) && !StringUtils.isEmpty(orderId)){
                break;
            }
        }
        try {
            redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).delete(id);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("Redis operation order_deleteRedis failed: " + e.getMessage());
        }

        List<OrderGoods> OrderGoodsList = (List<OrderGoods>)redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).get(orderId);
        for(OrderGoods orderGoods : OrderGoodsList){
            if(id.equals(orderGoods.getId())){
                OrderGoodsList.remove(orderGoods);
                try {
                    redisTemplate.boundHashOps(SystemConstants.redis_shopCartUser).put(orderId,OrderGoodsList);
                } catch (Exception e) {
                    log.error("Redis operation orderGoods_deleteRedis failed: " + e.getMessage());
                }
                break;
            }
        }
    }
}
