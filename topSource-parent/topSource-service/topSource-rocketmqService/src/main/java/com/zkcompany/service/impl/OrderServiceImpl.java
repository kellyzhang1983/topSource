package com.zkcompany.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zkcompany.dao.OrderGoodsMapper;
import com.zkcompany.dao.OrderMapper;
import com.zkcompany.entity.*;
import com.zkcompany.fegin.MarketCenterFegin;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.*;
import com.zkcompany.rocketMQ.SendMessageMQ;
import com.zkcompany.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService, UserDetailsService {


    @Autowired
    private UserCenterFegin userCenterFegin;

    @Autowired
    private MarketCenterFegin marketCenterFegin;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderGoodsMapper orderGoodsMapper;

    @Autowired
    private IdWorker idCreate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SendMessageMQ sendMessageMQ;

    /**
     * 监听MQ生成营销订单
     * ***/
    @GlobalTransactional
    @Override
    public void createMarketOrder(Order order) throws Exception{
        //添加订单信息，入库
        int reuslt = orderMapper.insertSelective(order);
        //增加积分，调用userCenterFegin增加用户积分
        if(reuslt > 0){
            Map<String, Object> point = create_point(order,200);
            Result result = userCenterFegin.addUserPointTimerTask(point);
            if(!result.isFlag()){
                throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
            }
        }
        //生成订单商品信息
        List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(order.getOrderActivity());
        for(ActivityGoods activityGoods : activityGoodsList){
            if(activityGoods.getGoodsId().equals(order.getGoodsId())){
                OrderGoods orderGoods = new OrderGoods();
                orderGoods.setId(String.valueOf(idCreate.nextId()));
                orderGoods.setOrderId(order.getId());
                orderGoods.setGoodsId(activityGoods.getGoodsId());
                orderGoods.setGoodsName(activityGoods.getGoodsName());
                orderGoods.setBrandName(activityGoods.getBrandName());
                orderGoods.setGoodsImage(activityGoods.getGoodsImage());
                orderGoods.setPrice(activityGoods.getPrice());
                orderGoods.setGoodsNum(1);
                orderGoods.setTotalPrice(activityGoods.getPrice().multiply(new BigDecimal(orderGoods.getGoodsNum())));
                orderGoods.setCreated(WorldTime.chinese_time(new Date()));
                orderGoods.setUpdated(WorldTime.chinese_time(new Date()));
                orderGoodsMapper.insertSelective(orderGoods);
                //减少营销商品库存信息
                Result result = marketCenterFegin.reduceActivityGoods(activityGoods);
                if(!result.isFlag()){
                    if(result.getCode() == 404){
                        order.setGrabStatus(2);
                        order.setDesc("商品已售罄！售罄的商品编号：" + orderGoods.getGoodsId());
                        saveMarkActivitySatus(order);
                        throw new RuntimeException(result.getMessage());
                    }else {
                        throw new RuntimeException("调用marketCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                    }

                }
            }
        }

        order.setGrabStatus(1);
        order.setDesc("已成功下单，订单编号：" + order.getId());
        saveMarkActivitySatus(order);

        //发送给延时队列处理，30分钟后订单自动关闭
        JSONObject josn_order = (JSONObject)JSONObject.toJSON(order);
        sendMessageMQ.SendMessage_async_delay(order,RocketMQInfo.rocketMQ_topic_orderProcessDelay,3000,9,josn_order);

    }
    /**
     * 监听MQ处理未支付订单
     * ***/
    @GlobalTransactional
    @Override
    public void cancelMarketOrder(Order order) throws Exception {
        //查看订单状态，只处理未支付的订单
        Order order_his = (Order)redisTemplate.boundHashOps(SystemConstants.redis_Order).get(order.getId());
        if(ObjectUtils.isEmpty(order_his)){
            return;
        }
        if(order_his.getOrderState().equals("1")){
            order.setOrderState("3");
            int reuslt = orderMapper.upateOrderStatus(order.getId(),order.getOrderState());
            //取消积分后，调用userCenterFegin取消用户积分
            if(reuslt > 0){
                Map<String, Object> point = create_point(order,-200);
                Result result = userCenterFegin.addUserPointTimerTask(point);
                if(!result.isFlag()){
                    throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                }
            }
            //只有营销活动商品会进行监听，所以需要只需要营销商品退回库存
            List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(order.getOrderActivity());
            for(ActivityGoods activityGoods : activityGoodsList) {
                if (activityGoods.getGoodsId().equals(order.getGoodsId())) {
                    Result result = marketCenterFegin.addActivityGoods(activityGoods);
                    if(!result.isFlag()){
                        throw new RuntimeException("调用marketCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                    }
                }
            }
        }
    }


    @GlobalTransactional
    public void saveMarkActivitySatus(Order order){
        Result result = marketCenterFegin.addActivityStatus(order);
        if(!result.isFlag()){
            throw new RuntimeException("调用saveMarkActivitySatus接口失败！请查看详细原因：" + result.getMessage());
        }
    }

    private Map<String,Object> create_point(Order order,Integer point){
        Map<String,Object> body = new HashMap<String,Object>();
        body.put("user_id",order.getUserId());
        body.put("change_type",3);
        body.put("points_detail",order.getId());
        body.put("point",point);
        return body;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

}
