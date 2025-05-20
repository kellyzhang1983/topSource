package com.zkcompany.controller;

import com.alibaba.nacos.shaded.com.google.common.collect.ForwardingMap;
import com.zkcompany.domain.LoginUser;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.User;
import com.zkcompany.service.UserAuthenticaitonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userAuthentication")
public class UserAuthenticationController {
    @Autowired
    private UserAuthenticaitonService userAuthenticaitonService;

    @PostMapping(value = "/login")
    public Result login(@RequestBody User user){
        String jwt_token = userAuthenticaitonService.loginUser(user.getUsername(), user.getPassword());
        Map<String,String> token = new HashMap<>();
        token.put("token",jwt_token);
        return new Result<>(true, StatusCode.SC_OK,"登录成功！",token);
    }

    @GetMapping("/update")
    @PreAuthorize("hasAnyRole('admin')")
    public String update(){
        return "你可以访问delete该接口呢";
    }

    @GetMapping("/loginOut")
    public Result loginOut(){
        try {
            userAuthenticaitonService.loginOut();
        } catch (Exception e) {
            return new Result<>(false, StatusCode.ERROR,"用户注销失败！请看查看异常情况",e.getMessage());
        }
        return new Result<>(true, StatusCode.SC_OK,"用户注销成功！");
    }

    @PostMapping ("/createUser")
    @PreAuthorize("hasAnyRole('admin')")
    public Result createUser(@RequestBody User user){
        UserDetails userDetails = new LoginUser(user,null);
        userAuthenticaitonService.createUser(userDetails);
        return new Result<>(true, StatusCode.SC_OK,"创建用户成功！");
    }
}
