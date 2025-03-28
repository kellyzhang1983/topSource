package com.zkcompany.dao;

import com.zkcompany.pojo.Point;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserPointMapper extends Mapper<Point> {

    @Select("SELECT user_id,sum(points_change) as points_change FROM `tb_user_points` GROUP BY user_id HAVING user_id = ${user_id}")
    Point findUserTotalPoint(String user_id);
}
