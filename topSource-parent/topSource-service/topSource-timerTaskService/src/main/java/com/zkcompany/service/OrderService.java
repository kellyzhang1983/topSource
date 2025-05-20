package com.zkcompany.service;

import com.zkcompany.entity.Result;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.ShopCart;
import com.zkcompany.pojo.User;

import java.util.List;

public interface OrderService {
    int closeOrder(String orderId) throws Exception;


}
