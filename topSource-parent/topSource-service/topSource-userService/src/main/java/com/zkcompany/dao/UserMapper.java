package com.zkcompany.dao;

import com.zkcompany.pojo.User;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<User> {
    // 可以添加自定义方法，通用 Mapper 会提供一些基础的 CRUD 操作
    @Select("select * from tb_user where status = #{status}")
    List<User> findUserState(String status);
}


