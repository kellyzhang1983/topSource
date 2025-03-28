package com.zkcompany.service;

import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.provisioning.UserDetailsManager;

public interface UserAuthenticaitonService extends UserDetailsManager,UserDetailsPasswordService {

    String loginUser(String username,String password);
/*
    void createUser(UserDetails user);

    void updateUser(UserDetails user);

    void deleteUser(String username);

    void changePassword(String oldPassword, String newPassword);

    UserDetails updatePassword(UserDetails user, String newPassword);*/

    void loginOut() throws Exception;
}
