package com.zkcompany.dao;

import com.zkcompany.pojo.ActivityGoods;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ActivityGoodsMapper extends Mapper<ActivityGoods> {

    @Select("select * from db_market.tb_activity_goods where market_id = #{marketId}")
    List<ActivityGoods> selectMarketActivityGoods(String marketId);
}
