package com.zkcompany.fallback;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
@Slf4j
public class UserServerFallBack implements UserCenterFegin {
    @Override
    public Result<User> findUser(String id) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"UserCenterFegin（user-server）：远程服务调用失败,请查看详细信息！.....");
    }

    @Override
    public Result<Map<String, Object>> findAllUserPage() {
        return null;
    }

    @Override
    public Result addUserPoint(Map<String, Object> body) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"UserCenterFegin（user-server）：远程服务调用失败,请查看详细信息！.....");
    }

    @Override
    public Result cancelPoint(String user_id, String order_id) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"UserCenterFegin（user-server）：远程服务调用失败,请查看详细信息！.....");
    }

    @Override
    public Result<User> findUserByUserName(String username) {
        return null;
    }

    @Override
    public Result addUser(String username, String password) {
        return null;
    }

    @Override
    public Result findUserStatus(String status) {
        return null;
    }

    @Override
    public Result addUserPointTimerTask(Map<String, Object> body) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"UserCenterFegin（addUserPointTimerTask）：远程服务调用失败,请查看详细信息！.....");
    }
}
