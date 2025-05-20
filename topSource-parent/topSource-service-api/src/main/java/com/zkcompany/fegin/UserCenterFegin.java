package com.zkcompany.fegin;


import com.zkcompany.annotation.InnerMethodCall;
import com.zkcompany.config.FeignApiConfig;
import com.zkcompany.entity.Result;
import com.zkcompany.fallback.UserServerFallBack;
import com.zkcompany.pojo.User;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-server",configuration = FeignApiConfig.class, fallback = UserServerFallBack.class)
public interface UserCenterFegin {
    @GetMapping("/user/findUser")
    public Result<User> findUser(@RequestParam(value = "id")  String id);

    @PostMapping("/user/findAllUserPage")
    public Result<Map<String, Object>> findAllUserPage();

    @PostMapping(value = "/point/addUserPoint")
    public Result addUserPoint(@RequestBody Map<String,Object> body);

    @GetMapping(value = "/point/cancelPoint")
    public Result cancelPoint(@RequestParam(value = "user_id") String user_id,
                              @RequestParam(value = "order_id") String order_id);

    @GetMapping("/user/findUserByUserName")
    public Result<User> findUserByUserName(@RequestParam (value = "username")  String username);

    @GetMapping("/user/addUser")
    public Result addUser(@RequestParam (value = "username")  String username,
                          @RequestParam (value = "password")  String password);

    @GetMapping("/user/findUserStatus")
    public Result findUserStatus(@RequestParam(value = "status") String status);

    @PostMapping(value = "/point/addUserPointTimerTask")
    @InnerMethodCall
    public Result addUserPointTimerTask(@RequestBody Map<String,Object> body);
}
