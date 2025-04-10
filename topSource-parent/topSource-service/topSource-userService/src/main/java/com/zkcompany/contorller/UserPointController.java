package com.zkcompany.contorller;


import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.Point;
import com.zkcompany.pojo.User;
import com.zkcompany.service.UserPointService;
import com.zkcompany.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/point")
@RestController
public class UserPointController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPointService userPointService;

    @PreAuthorize("hasAnyRole('user')")
    @PostMapping(value = "/addUserPoint")
    public Result addUserPoint(@RequestBody Map<String,Object> body){

        if (body == null || body.isEmpty()){
            throw new BusinessException(StatusCode.SC_NOT_FOUND,"请将必要的参数传过来！");
        }else{
            String user_id = body.get("user_id") == null? "" : body.get("user_id").toString();
            if (StringUtils.isEmpty(user_id)){
                throw new BusinessException(StatusCode.SC_NOT_FOUND,"请将user_id参数传过来！");
            }

            String point = body.get("point") == null ? "" : body.get("point").toString();
            if (StringUtils.isEmpty(point)){
                throw new BusinessException(StatusCode.SC_NOT_FOUND,"请将point参数传过来！");
            }

            String change_type = body.get("change_type") == null ? "" : body.get("change_type").toString();
            if (StringUtils.isEmpty(change_type)){
                throw new BusinessException(StatusCode.SC_NOT_FOUND,"请将change_type参数传过来！");
            }

            String points_detail = body.get("points_detail") == null ? "" : body.get("points_detail").toString();
            if (StringUtils.isEmpty(points_detail)){
                throw new BusinessException(StatusCode.SC_NOT_FOUND,"请将points_detail参数传过来！");
            }
        }

        User user = userService.findUser(body.get("user_id").toString());
        if (!(user == null || "".equals(user))){
            userPointService.addUserPoint(body);
            return new Result(true, StatusCode.SC_OK,"操作积分成功！");
        }else {
            return new Result(false, StatusCode.SC_NOT_FOUND,"没有该用户，不能操作积分！");
        }
    }

    @GetMapping(value = "/findUserPoint")
    public Result findUserPoint(@RequestParam(value = "user_id") String user_id){
        List<Point> userPoint = userPointService.findUserPoint(user_id);
        return new Result(true, StatusCode.OK,"查询用户积分明细成功！",userPoint);
    }

    @GetMapping(value = "/findUserTotalPoint")
    public Result findUserTotalPoint(@RequestParam(value = "user_id") String user_id){

        Integer userTotalPoint = userPointService.findUserTotalPoint(user_id);
        if(userTotalPoint == 0){
            return new Result(false, StatusCode.SC_NOT_FOUND,"没找到该用户！");
        }else {
            return new Result(true, StatusCode.SC_OK,"查询用户总积分成功！",userTotalPoint);
        }
    }

    @PreAuthorize("hasAnyRole('user')")
    @GetMapping(value = "/cancelPoint")
    public Result cancelPoint(@RequestParam(value = "user_id") String user_id,
                              @RequestParam(value = "order_id") String order_id){
        int userTotalPoint = userPointService.cancelPonit(user_id, order_id);
        if(userTotalPoint == 0){
            return new Result(false, StatusCode.SC_NOT_FOUND,"没找到该用户所对应的积分！......");
        }else {
            return new Result(true, StatusCode.SC_OK,"取消积分成功！......");
        }
    }
}
