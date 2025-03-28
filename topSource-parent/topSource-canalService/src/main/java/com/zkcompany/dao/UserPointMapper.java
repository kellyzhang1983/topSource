package com.zkcompany.dao;

import com.zkcompany.pojo.Point;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserPointMapper extends Mapper<Point> {

    @Select("select * from db_user.tb_user_points where user_id = #{user_id}")
    List<Point> searchUserPoint(String user_id);

    @Select("SELECT DISTINCT user_id AS user_id FROM db_user.tb_user_points")
    List<Point> checkTbUserId();

}
