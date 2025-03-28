package com.zkcompany.service.impl;

import com.zkcompany.dao.UserMapper;
import com.zkcompany.dao.UserOrderMapper;
import com.zkcompany.dao.UserPointMapper;
import com.zkcompany.dao.UserRoleMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.Point;
import com.zkcompany.pojo.User;
import com.zkcompany.pojo.UserRole;
import com.zkcompany.service.CheckUserData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class CheckUserDataImpl implements CheckUserData {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserPointMapper userPointMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserOrderMapper userOrderMapper;

    @Override
    public boolean check_tb_user() {
        log.info("检查db_user库（tb_user表开始）........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_userInfo).keys();
        List<User> userList = userMapper.selectAll();
        boolean sync = true;
        if(keys.size() != userList.size()){
            log.info("开始同步到Redis中........");
            redisTemplate.delete(SystemConstants.redis_userInfo);
            int count = 0;
            for(User user : userList){
                redisTemplate.boundHashOps(SystemConstants.redis_userInfo).put(user.getId(),user);
                count ++;
            }
            sync = false;
            log.info("开始同步到Redis中........共同步了：" + count + "条数据！");
        }
        return sync;
    }

    @Override
    public boolean check_tb_user_point() {
        log.info("检查db_user库（tb_user_points表开始）........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_userPoint).keys();
        List<Point> pointList = userPointMapper.checkTbUserId();
        boolean sync = true;
        int count = 0;
        if(keys.size() != pointList.size()){
            log.info("开始删除Redis中的userPoint数据........");
            sync = false;
            for (Object key : keys) {
                Long delete = redisTemplate.boundHashOps(SystemConstants.redis_userPoint).delete(key);
                count = count + Integer.valueOf(delete.toString());
            }
            log.info("总共删除Redis中的userPoint数据：" + count + "条数据！");
        }
        count = 0;
        for(Point point : pointList){
            List<Point> points_mysql = userPointMapper.searchUserPoint(point.getUser_id());
            List<Point> points_redis = (List<Point>)redisTemplate.boundHashOps(SystemConstants.redis_userPoint).get(point.getUser_id());
            if(points_mysql.size() != (points_redis == null ? 0 : points_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中........");
                    sync = false;
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userPoint).put(point.getUser_id(), points_mysql);
                count ++;

            }

        }

        if(count != 0) {
            log.info("开始同步到Redis中........共同步了：" + count + "条数据！");
        }
        return sync;
    }


    @Override
    public boolean check_tb_user_role(){
        log.info("检查db_user库（tb_user_role表开始）........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).keys();
        List<UserRole> userRoleList = userRoleMapper.selectAllRole();
        boolean sync = true;
        int count = 0;
        if(keys.size() != userRoleList.size()){
            log.info("开始删除Redis中的userRole数据........");
            sync = false;
            for (Object key : keys) {
                Long delete = redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).delete(key);
                count = count + Integer.valueOf(delete.toString());
            }
            log.info("总共删除Redis中的userRole数据：" + count + "条数据！");
        }
        count = 0;
        for(UserRole userRole : userRoleList){
            List<String> userRoles = new ArrayList<String>();
            List<UserRole> userRoles_mysql = userRoleMapper.selectRole(userRole.getUser_id());
            List<String> userRoles_redis = (List<String>)redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).get(userRole.getUser_id());
            if(userRoles_mysql.size() != (userRoles_redis == null ? 0 : userRoles_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中........");
                    sync = false;
                }
                for(UserRole userRole_mysql : userRoles_mysql){
                    String userRoleString = "ROLE_" + userRole_mysql.getRole_name();
                    userRoles.add(userRoleString);
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).put(userRole.getUser_id(), userRoles);
                count ++;

            }

        }

        if(count != 0) {
            log.info("开始同步到Redis中........共同步了：" + count + "条数据！");
        }
        return sync;
    }

    @Override
    public boolean check_tb_order() {
        log.info("检查db_order库（tb_order表开始）........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_Order).keys();
        List<Order> orderList = userOrderMapper.selectAll();
        boolean sync = true;
        if(keys.size() != orderList.size()){
            log.info("开始同步到Redis中........");
            redisTemplate.delete(SystemConstants.redis_Order);
            int count = 0;
            for(Order order : orderList){
                redisTemplate.boundHashOps(SystemConstants.redis_Order).put(order.getId(),order);
                count ++;
            }
            sync = false;
            log.info("开始同步到Redis中........共同步了：" + count + "条数据！");
        }
        return sync;
    }


    @Override
    public boolean check_tb_userOrder(){
        log.info("检查db_order库（tb_order表开始）........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_userOrder).keys();
        List<Order> orderList = userOrderMapper.selectAllUserOrder();
        boolean sync = true;
        int count = 0;
        if(keys.size() != orderList.size()){
            log.info("开始删除Redis中的userOrder数据........");
            sync = false;
            for (Object key : keys) {
                Long delete = redisTemplate.boundHashOps(SystemConstants.redis_userOrder).delete(key);
                count = count + Integer.valueOf(delete.toString());
            }
            log.info("总共删除Redis中的userOrder数据：" + count + "条数据！");
        }
        count = 0;
        for(Order order : orderList){
            List<Order> userOrder_mysql = userOrderMapper.searchUserOrder(order.getUser_id());
            List<Order> userOrder_redis = (List<Order>)redisTemplate.boundHashOps(SystemConstants.redis_userOrder).get(order.getUser_id());
            if(userOrder_mysql.size() != (userOrder_redis == null ? 0 : userOrder_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中........");
                    sync = false;
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userOrder).put(order.getUser_id(), userOrder_mysql);
                count ++;

            }

        }

        if(count != 0) {
            log.info("开始同步到Redis中........共同步了：" + count + "条数据！");
        }
        return sync;
    }
}
