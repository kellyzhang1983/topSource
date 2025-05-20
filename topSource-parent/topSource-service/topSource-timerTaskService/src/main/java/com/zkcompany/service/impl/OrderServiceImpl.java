package com.zkcompany.service.impl;

import com.zkcompany.dao.OrderMapper;
import com.zkcompany.entity.*;
import com.zkcompany.fegin.GoodsCenterFegin;
import com.zkcompany.fegin.MarketCenterFegin;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.*;
import com.zkcompany.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService, UserDetailsService {


    @Autowired
    private UserCenterFegin userCenterFegin;

    @Autowired
    private MarketCenterFegin marketCenterFegin;

    @Autowired
    private GoodsCenterFegin goodsCenterFegin;


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;




    @GlobalTransactional
    @Override
    public int closeOrder(String orderId) throws Exception{
        int resultCode = 0;
        //1、查詢此定单
        Order order = (Order)redisTemplate.boundHashOps(SystemConstants.redis_Order).get(orderId);
        if(ObjectUtils.isEmpty(order)){
            resultCode = 6;
            return resultCode;
        }
        if(order.getOrderState().equals("1")){
            order.setOrderState("3");
            //根据定单ID修改订单状态
            int reuslt = orderMapper.upateOrderStatus(order.getId(),order.getOrderState());
            if(reuslt > 0){
                //调用userCenterFegin增加订单积分
                Map<String, Object> point = create_point(order,-200);
                Result result = userCenterFegin.addUserPointTimerTask(point);
                if(!result.isFlag()){
                    redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityOrderService_message).set("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                    throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                }
            }
            //2.回退商品库存
            try {
                fallbackGoodNum(order);
            } catch (Exception e) {
                throw new RuntimeException("调用fallbackGoodNum回退商品库存失败！请查看详细原因：" + e.getMessage());
            }

            //3.回退商品销量
            try {
                fallbackGoodSaleNum(order);
            } catch (Exception e) {
                throw new RuntimeException("调用fallbackGoodSaleNum回退商品销量失败！请查看详细原因：" + e.getMessage());
            }

            resultCode = 2;
            return resultCode;
        }else{
            resultCode = Integer.valueOf(order.getOrderState()) ;
            return resultCode;
        }
    }

    public void fallbackGoodNum(Order order) throws Exception{
        List<OrderGoods> orderGoodsList =(List<OrderGoods>) redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).get(order.getId());
            for(OrderGoods orderGoods : orderGoodsList) {
                Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(orderGoods.getGoodsId());
                if (StringUtils.isEmpty(order.getOrderActivity())) {
                    goods.setNum(orderGoods.getGoodsNum());
                    Result result = goodsCenterFegin.updateGoodsNumInventoryTimeTask(goods,"add");
                    if (!result.isFlag()) {
                        throw new RuntimeException("调用goodsCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                    }
                }else{
                    List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(order.getOrderActivity());
                    for(ActivityGoods activityGoods : activityGoodsList){
                        if(activityGoods.getGoodsId().equals(orderGoods.getGoodsId())){
                            Result result = marketCenterFegin.addActivityGoods(activityGoods);
                            if(!result.isFlag()){
                                throw new RuntimeException("调用marketCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                            }

                        }
                    }
                }
            }
    }

    public void fallbackGoodSaleNum(Order order) throws Exception{
        List<OrderGoods> orderGoodsList =(List<OrderGoods>) redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).get(order.getId());
        if(StringUtils.isEmpty(order.getOrderActivity())) {
            for (OrderGoods orderGoods : orderGoodsList) {
                Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(orderGoods.getGoodsId());
                goods.setSaleNum(goods.getSaleNum() - orderGoods.getGoodsNum());
                Result result = goodsCenterFegin.upateGoodsNumTimerTask(goods);
                if (!result.isFlag()) {
                    throw new RuntimeException("调用goodsCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                }
            }
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
