package com.zkcompany.service;

import com.zkcompany.entity.Result;
import com.zkcompany.pojo.Order;

public interface SendMessageService {



    Result orderSendMessage(Order order) throws Exception;
}