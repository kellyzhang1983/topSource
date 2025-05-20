package com.zkcompany.service;

import com.zkcompany.entity.Result;
import com.zkcompany.pojo.*;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Result<User> searchUser(String id) throws Exception;

    Result createOrder(String userId, List<ShopCart> shopCarts) throws Exception;

    List<Order> selectUserOrder(String userId) throws Exception;

    int payOrderStatus(String orderId) throws Exception;

    int refundOrder(String orderId) throws Exception;

    Result placeMarketOrder(String userId, ActivityGoods activityGoods) throws Exception;

    void fallbackGoodNum(Order order) throws Exception;

    void fallbackGoodSaleNum(Order order) throws Exception;



}
