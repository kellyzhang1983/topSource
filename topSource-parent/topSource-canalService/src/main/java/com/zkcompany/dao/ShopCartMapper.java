package com.zkcompany.dao;

import com.zkcompany.pojo.Goods;
import com.zkcompany.pojo.ShopCart;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ShopCartMapper extends Mapper<ShopCart> {

    @Select("select * from tb_shopping_cart where user_id = #{userId}")
    List<ShopCart> selectShopCartUser(String userId);
}
