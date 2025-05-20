package com.zkcompany.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.dao.ShopCartMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.ShopCart;
import com.zkcompany.service.ProcessShopCartData;
import com.zkcompany.uitl.ConvertObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
public class SynShopCartData implements ProcessShopCartData {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ShopCartMapper shopCartMapper;

    @Override
    public void shopCart_addAndUpdateRedis(List<CanalEntry.Column> columns) {
        ShopCart shopCart = new ShopCart();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            //得到列的值
            String value = column.getValue();
            //转换成USER对象
            ConvertObject.convertShopCart(shopCart,name,value);
        }

        try {
            redisTemplate.boundHashOps(SystemConstants.redis_shopCartList).put(shopCart.getId(),shopCart);
        } catch (Exception e) {
            log.error("Redis operation shopCart_addAndUpdateRedis failed: " + e.getMessage());
        }

        List<ShopCart> shopCarts = shopCartMapper.selectShopCartUser(shopCart.getUserId());

        try {
            redisTemplate.boundHashOps(SystemConstants.redis_shopCartUser).put(shopCart.getUserId(),shopCarts);
        } catch (Exception e) {
            log.error("Redis operation shopCart_addAndUpdateRedis failed: " + e.getMessage());
        }


    }

    @Override
    public void shopCart_deleteRedis(List<CanalEntry.Column> columns) {
        String id = "";
        String userId = "";

        for (CanalEntry.Column column : columns){
            if(column.getName().equals("id")){
                id = column.getValue();
            }
            if(column.getName().equals("user_id")){
                userId = column.getValue();
            }
            if(!StringUtils.isEmpty(id) && !StringUtils.isEmpty(userId)){
                break;
            }
        }
        try {
            redisTemplate.boundHashOps(SystemConstants.redis_shopCartList).delete(id);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("Redis operation shopCart_deleteRedis failed: " + e.getMessage());
        }
        List<ShopCart> shopCarts = (List<ShopCart>)redisTemplate.boundHashOps(SystemConstants.redis_shopCartUser).get(userId);
        for(ShopCart shopCart : shopCarts){
            if(id.equals(shopCart.getId())){
                shopCarts.remove(shopCart);
                try {
                    redisTemplate.boundHashOps(SystemConstants.redis_shopCartUser).put(userId,shopCarts);
                } catch (Exception e) {
                    log.error("Redis operation shopCart_deleteRedis failed: " + e.getMessage());
                }
                break;
            }
        }
    }

}
