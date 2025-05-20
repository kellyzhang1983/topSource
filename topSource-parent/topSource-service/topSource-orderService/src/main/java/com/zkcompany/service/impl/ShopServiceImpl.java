package com.zkcompany.service.impl;

import com.zkcompany.dao.ShopCartMapper;
import com.zkcompany.entity.*;
import com.zkcompany.pojo.Goods;
import com.zkcompany.pojo.ShopCart;
import com.zkcompany.service.ShopCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ShopServiceImpl implements ShopCartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ShopCartMapper shopCartMapper;

    @Autowired
    private IdWorker idCreate;

    @Override
    public ShopCart addOrUpdateGoodsToShopCart(String userId,ShopCart shopCart,String operationMethod) throws Exception {

        String goodsId = shopCart.getGoodsId();
        Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(goodsId);


        shopCart.setId(String.valueOf(idCreate.nextId()));
        shopCart.setUserId(userId);
        shopCart.setGoodsName(goods.getName());
        shopCart.setBrandName(goods.getBrandName());
        shopCart.setGoodsImage(goods.getImage());
        shopCart.setPrice(goods.getPrice());
        shopCart.setTotalPrice(goods.getPrice().multiply(new BigDecimal(shopCart.getGoodsNum())));
        shopCart.setCreated(WorldTime.chinese_time(new Date()));
        shopCart.setUpdated(WorldTime.chinese_time(new Date()));

        //如果购物车已有该商品，那么直接修改购买数量，如果没有该商品，那么新增一条记录.
        List<ShopCart> shopCartsList = (List<ShopCart>)redisTemplate.boundHashOps(SystemConstants.redis_shopCartUser).get(userId);
        Boolean isAddGoodShopCart = true;
        shopCartsList = shopCartsList == null ? new ArrayList<ShopCart>() : shopCartsList;
        for (ShopCart shopCarts : shopCartsList){
            if(shopCarts.getGoodsId().equals(shopCart.getGoodsId())){
                Integer newGoodsNum = 0;
                if(operationMethod.equals("add")){
                    newGoodsNum = shopCarts.getGoodsNum() + shopCart.getGoodsNum();
                }else if(operationMethod.equals("subtraction")){
                    newGoodsNum = shopCarts.getGoodsNum() - shopCart.getGoodsNum();
                    if(newGoodsNum < 0){
                        throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,"【orderServerError：ShopCartController】shopCartService.addOrUpdateGoodsToShopCart:商品数量小于0！");
                    }
                }
                shopCart.setId(shopCarts.getId());
                shopCart.setGoodsNum(newGoodsNum);
                shopCart.setPrice(goods.getPrice());
                shopCart.setTotalPrice(goods.getPrice().multiply(new BigDecimal(newGoodsNum)));
                isAddGoodShopCart = false;
                //如果数量大于0.那么购物车修改商品数量，如果数量为0，那么购物车会将删除这条数据！
                if(newGoodsNum > 0){
                    shopCartMapper.updateByPrimaryKeySelective(shopCart);
                }else if (newGoodsNum == 0) {
                    shopCartMapper.deleteByPrimaryKey(shopCart);
                }
                break;
            }
        }

        if(isAddGoodShopCart){
            shopCartMapper.insertSelective(shopCart);
        }
        return shopCart;
    }

    @Override
    public List<ShopCart> selectUserShopCart(String userId) throws Exception {
        //从购物车读取商品信息，需要更新商品列表的价格，使价格同步！
        List<ShopCart> shopCartsList = (List<ShopCart>)redisTemplate.boundHashOps(SystemConstants.redis_shopCartUser).get(userId);
        shopCartsList = shopCartsList == null ? new ArrayList<ShopCart>() : shopCartsList;
        for (ShopCart shopCarts : shopCartsList) {
            Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(shopCarts.getGoodsId());
            shopCarts.setPrice(goods.getPrice());
            shopCarts.setTotalPrice(goods.getPrice().multiply(new BigDecimal(shopCarts.getGoodsNum())));
        }
        return shopCartsList;
    }
}
