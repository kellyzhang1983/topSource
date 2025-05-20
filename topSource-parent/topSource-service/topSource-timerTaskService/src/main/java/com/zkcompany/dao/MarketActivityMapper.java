package com.zkcompany.dao;

import com.zkcompany.pojo.MarketActivity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MarketActivityMapper extends Mapper<MarketActivity> {

    @Select("select * from db_market.tb_market_activity")
    List<MarketActivity> selectMarketActivity();


}
