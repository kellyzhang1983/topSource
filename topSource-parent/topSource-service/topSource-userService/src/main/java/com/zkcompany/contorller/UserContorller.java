package com.zkcompany.contorller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.User;
import com.zkcompany.service.UserService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/user")
@RestController
public class UserContorller {
    @Autowired
    private UserService userService;

    @GetMapping("/addUser")
    @PreAuthorize("hasAnyRole('admin')")
    public Result addUser(@RequestParam (value = "username")  String username,
                          @RequestParam (value = "password")  String passsword,
                          HttpServletRequest  request){
        String ip = request.getRemoteAddr();
        userService.addUser(username,passsword,ip);
        return new Result(true, StatusCode.SC_OK,"添加用户成功");
    }

    @PreAuthorize("hasAnyRole('user')")
    @GetMapping("/findUser")
    public Result<User> findUser(@RequestParam (value = "id")  String id){
        /*try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        User user = userService.findUser(id);
        if(ObjectUtils.isEmpty(user)){
            return new Result<User>(false, StatusCode.SC_NOT_FOUND,"没有找到该用户",user);
        }else {
            return new Result<User>(true, StatusCode.SC_OK,"查询用户成功",user);
        }

    }

    @GetMapping("/findUserByUserName")
    public Result<User> findUserByUserName(@RequestParam (value = "username")  String username){
        User user = userService.findUserByUserName(username);
        if(user == null || "".equals(user)){
            return new Result<User>(true, StatusCode.OK,"没有找到该用户");
        }else {
            return new Result<User>(true, StatusCode.OK,"查询用户成功",user);
        }

    }

    @PreAuthorize("hasAnyRole('user','admin')")
    @PostMapping("/findAllUserPage")
    public Result<Map<String, Object>> findAllUserPage(@RequestBody(required = false) Map<String,Object> body){
        Map<String, Object> resultMap = userService.findAllUserPage(body);
        return new Result<Map<String, Object>>(true, StatusCode.OK,"查询用户成功",resultMap);
    }


    @GetMapping("/deleteUser")
    public Result deleteUser(@RequestParam (value = "id")  String id){
        int i = userService.deleteUser(id);
        if (i == 0) {
            return new Result(true, StatusCode.OK, "没找到该用户...");
        }else {
            return new Result(true, StatusCode.OK, "删除该用户");
        }
    }

    @PreAuthorize("hasAnyRole('user')")
    @Bulkhead(name="userServer_BulkheadInstance", fallbackMethod = "bulkheadError",type = Bulkhead.Type.SEMAPHORE)
    @GetMapping("/findUserStatus")
    public Result findUserStatus(@RequestParam(value = "status") String status){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<User> userList = userService.findUserState(status);
        return new Result(true,StatusCode.OK,"查询用户成功",userList);
    }

    public Result bulkheadError(Exception e) {
        return new Result<>(false, StatusCode.REMOTEERROR,"userServer_Bulkhead：流量超出最大限制！系统繁忙，请稍后再试.....",e.getMessage());
    }

}
