package com.zkcompany.dao;

import com.zkcompany.pojo.Goods;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface GoodsMapper extends Mapper<Goods> {

    @Update("update tb_goods set goods_num = goods_num + #{goods_num} where id = #{id}")
    int addGoodsNumInventory(Integer goods_num,String id);

    @Update("update tb_goods set goods_num = goods_num - #{goods_num} where id = #{id}")
    int reduceGoodsNumInventory(Integer goods_num,String id);
}
