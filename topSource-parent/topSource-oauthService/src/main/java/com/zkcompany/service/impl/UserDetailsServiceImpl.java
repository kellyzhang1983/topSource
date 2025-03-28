package com.zkcompany.service.impl;


import com.alibaba.fastjson2.JSON;
import com.zkcompany.dao.UserMapper;
import com.zkcompany.dao.UserRoleMapper;
import com.zkcompany.domain.LoginUser;
import com.zkcompany.entity.*;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.User;
import com.zkcompany.pojo.UserRole;
import com.zkcompany.service.UserAuthenticaitonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserAuthenticaitonService {

    private List<String> userRoleString;

    private User user;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    //没太明白为什么要加延迟加载、这表明userDetailsServiceImpl依赖于authenticationManager，同时authenticationManager又依赖于userDetailsServiceImpl，形成了循环依赖。
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public String loginUser(String username, String password) {
        //Security 自己的验证框架，只需要传入用户名、密码
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username,password);
        //这个方法还是会执行loadUserByUsername方法，Security自己的验证框架。
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        //得到用户信息
        LoginUser loginUser = (LoginUser)authenticate.getPrincipal();

        if(Objects.isNull(loginUser)){
            redisTemplate.boundValueOps(SystemConstants.redis_errorSecurity_message).set("用户名或密码错误！");
            throw new UsernameNotFoundException("");
        }
        //通过框架验证后，得到用户信息。

        User user = loginUser.getUser();
        //去掉密码。
        user.setPassword("");
        //封装成JWT，把权限放进去
        Map<String,Object> jwtMap = new HashMap<String,Object>();
        jwtMap.put("user",user);
        jwtMap.put("user_role", userRoleString == null ? new ArrayList<String>(): userRoleString);
        String jwtMap_jsonString = JSON.toJSONString(jwtMap);

        //利用JWT，得到Token、token有效期初步定能为7天
        String jwt = JwtUtil.createJWT(jwtMap_jsonString, Long.valueOf(7*24*60*60*1000));
        //把token加入redis缓存中。如果注销，删除缓存中的token
        redisTemplate.boundHashOps(SystemConstants.redis_userToken).put(user.getUsername(),jwt);
        return jwt;
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        boolean userExists = userExists(username);
        //用户是否为空
        if(userExists){
            redisTemplate.boundValueOps(SystemConstants.redis_errorSecurity_message).set("用户名或密码错误！");
            throw new UsernameNotFoundException("");
        }

        return new LoginUser(user,userRoleString);
    }


    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        //查询用户信息
        Boolean userExists = false;
        //数据库查询判断用户是否存在。
        user = userMapper.selectByUsername(username);

        if(Objects.isNull(user)){
            //如果用户不存在，则设置为false。
            userExists = true;
        }else {
            //得到角色
            //从Redis中取出角色数据。数据结构Map<String,list<String>>
            userRoleString = (List<String>) redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).get(user.getId());
            if(userRoleString == null || userRoleString.size() == 0){
                //从数据库中取出角色数据。数据结构Map<String，UserRole对象>、需要转换成Map<String,list<String>>
                userRoleString = (List<String>)userRoleMapper.selectRole(user.getId());
                if(Objects.isNull(userRoleString)){
                    userRoleString = new ArrayList<String>();
                }else{
                    userRoleString.replaceAll(element -> "ROLE_" + element);
                }

            }

        }
        return userExists;
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return null;
    }

    @Override
    public void loginOut() throws Exception{
        //从容器的过滤器中的上下文获得用户信息。
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        //得到用户USERNAME
        User user_principal = (User)authentication.getPrincipal();
        //Redis中删除用户的token
        redisTemplate.boundHashOps(SystemConstants.redis_userToken).delete(user_principal.getUsername());
    }


}
