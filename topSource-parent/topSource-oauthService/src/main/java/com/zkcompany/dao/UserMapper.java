package com.zkcompany.dao;

import com.zkcompany.pojo.User;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User> {

    @Select("select * from tb_user where username = #{username}")
    User selectByUsername(String username);
}
