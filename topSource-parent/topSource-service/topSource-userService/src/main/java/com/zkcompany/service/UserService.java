package com.zkcompany.service;

import com.zkcompany.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    boolean addUser(String userName,String password,String ip);

    Map<String, Object> findAllUserPage(Map<String,Object>  body);

    User findUser(String id);

    int deleteUser(String id);

    List<User> findUserState(String status);

    User findUserByUserName(String userName);
}
