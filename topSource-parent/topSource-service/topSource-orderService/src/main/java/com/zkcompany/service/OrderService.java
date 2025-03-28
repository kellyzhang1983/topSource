package com.zkcompany.service;

import com.zkcompany.entity.Result;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.User;

import java.util.Map;

public interface OrderService {
    Result<User> searchUser(String id) throws Exception;

    Result<User> addUserPoint(String user_id) throws Exception;
    int paySatus(Order order) throws Exception;

    int cancelOrder(Order order) throws Exception;

    int cretaOrder(Order order) throws Exception;

    String placeOrder(String user_id) throws Exception;

    Order searchOrder(String order_id) throws Exception;
}
