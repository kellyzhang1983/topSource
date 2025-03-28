package com.zkcompany.dao;

import com.zkcompany.pojo.Order;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserOrderMapper extends Mapper<Order> {

    @Select("select * from db_order.tb_order where user_id = #{user_id}")
    List<Order> searchUserOrder(String user_id);

    @Select("SELECT DISTINCT user_id AS user_id FROM db_order.tb_order")
    List<Order> selectAllUserOrder();
}
