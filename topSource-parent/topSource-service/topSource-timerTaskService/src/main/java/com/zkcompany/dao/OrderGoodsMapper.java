package com.zkcompany.dao;

import com.zkcompany.pojo.OrderGoods;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderGoodsMapper extends Mapper<OrderGoods> {

    @Select("select * from tb_order_goods where order_id = #{orderId}")
    List<OrderGoods> selectOrderGoods(String orderId);

    @Select("select DISTINCT order_id as order_id from tb_order_goods")
    List<OrderGoods> selectOrder();
}
