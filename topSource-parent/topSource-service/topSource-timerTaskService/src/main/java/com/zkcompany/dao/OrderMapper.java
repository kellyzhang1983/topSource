package com.zkcompany.dao;

import com.zkcompany.pojo.Order;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface OrderMapper extends Mapper<Order> {

    @Update("update tb_order set order_state = #{status} where id = #{orderId}")
    int upateOrderStatus(String orderId,String status);

}
