package com.zkcompany.dao;


import com.zkcompany.pojo.User;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<User> {

    @Select("SELECT * FROM db_user.tb_user")
    List<User> selectAll();
}
