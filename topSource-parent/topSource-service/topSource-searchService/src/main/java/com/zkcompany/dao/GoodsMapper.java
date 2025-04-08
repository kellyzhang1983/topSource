package com.zkcompany.dao;

import com.zkcompany.pojo.Goods;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface GoodsMapper extends Mapper<Goods> {

    @Select("select * from db_goods.tb_sku")
    List<Goods> selectAllGoods();

}
