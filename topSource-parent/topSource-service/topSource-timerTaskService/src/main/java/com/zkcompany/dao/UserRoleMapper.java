package com.zkcompany.dao;

import com.zkcompany.pojo.UserRole;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserRoleMapper extends Mapper<UserRole> {

    @Select("SELECT r.role_name as roleName FROM db_user.tb_user_roles ur,db_user.tb_roles r " +
            "where  ur.role_id = r.role_id " +
            "and ur.user_id = #{user_id}")
    List<UserRole> selectRole(String user_id);

    @Select("SELECT DISTINCT user_id AS user_id FROM db_user.tb_user_roles")
    List<UserRole> selectAllRole();


}
