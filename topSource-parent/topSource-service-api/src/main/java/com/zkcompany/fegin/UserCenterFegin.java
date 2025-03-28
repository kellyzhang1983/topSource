package com.zkcompany.fegin;


import com.zkcompany.config.FeignConfig;
import com.zkcompany.entity.Result;
import com.zkcompany.fallback.UserServerFallBack;
import com.zkcompany.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-server",configuration = FeignConfig.class, fallback = UserServerFallBack.class)
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

    @GetMapping("/findUserStatus")
    public Result findUserStatus(@RequestParam(value = "status") String status);
}
