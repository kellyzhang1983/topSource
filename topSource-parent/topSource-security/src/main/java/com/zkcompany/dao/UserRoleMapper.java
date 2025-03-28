package com.zkcompany.dao;

import com.zkcompany.pojo.UserRole;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserRoleMapper extends Mapper<UserRole> {

    @Select("SELECT r.role_name as role_name FROM tb_user_roles ur,tb_roles r " +
            "where  ur.role_id = r.role_id " +
            "and ur.user_id = #{user_id}")
    List<String> selectRole(String user_id);

    @Select("SELECT role_id from tb_roles where role_name = #{role_name}")
    String selectRoleID(String role_name);
}
