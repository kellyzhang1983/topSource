package com.zkcompany.dao;

import com.zkcompany.pojo.ActivityStatus;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ActivityStatusMapper extends Mapper<ActivityStatus> {

    @Select("select * from db_market.tb_activity_status where user_id = #{userId}")
    List<ActivityStatus> selectActivityStatus(String userId);

}
