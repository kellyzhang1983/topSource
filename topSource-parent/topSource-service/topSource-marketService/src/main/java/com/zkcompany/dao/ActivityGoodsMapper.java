package com.zkcompany.dao;

import com.zkcompany.pojo.ActivityGoods;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ActivityGoodsMapper extends Mapper<ActivityGoods> {

    @Update("update tb_activity_goods set goods_num = 0 where id = #{id}")
    int reduceGoodsNum(String id);

    /*@Update("update tb_activity_goods set goods_num = goods_num + 1 where id = #{id}")
    int addGoodsNum(String id);*/

    @Select("select goods_num from tb_activity_goods where market_id = #{marketId} and goods_id = #{goodsId}")
    int selectGoodsNum(String marketId,String goodsId);
}
