package com.zkcompany.service.impl;

import com.zkcompany.dao.*;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.*;
import com.zkcompany.service.CheckData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class CheckDataImpl implements CheckData {

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

    @Autowired
    private GoodsMapper goodsMapper;

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
            List<Point> points_mysql = userPointMapper.searchUserPoint(point.getUserId());
            List<Point> points_redis = (List<Point>)redisTemplate.boundHashOps(SystemConstants.redis_userPoint).get(point.getUserId());
            if(points_mysql.size() != (points_redis == null ? 0 : points_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中........");
                    sync = false;
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userPoint).put(point.getUserId(), points_mysql);
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
            List<UserRole> userRoles_mysql = userRoleMapper.selectRole(userRole.getUserId());
            List<String> userRoles_redis = (List<String>)redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).get(userRole.getUserId());
            if(userRoles_mysql.size() != (userRoles_redis == null ? 0 : userRoles_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中........");
                    sync = false;
                }
                for(UserRole userRole_mysql : userRoles_mysql){
                    String userRoleString = "ROLE_" + userRole_mysql.getRoleName();
                    userRoles.add(userRoleString);
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).put(userRole.getUserId(), userRoles);
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
            List<Order> userOrder_mysql = userOrderMapper.searchUserOrder(order.getUserId());
            List<Order> userOrder_redis = (List<Order>)redisTemplate.boundHashOps(SystemConstants.redis_userOrder).get(order.getUserId());
            if(userOrder_mysql.size() != (userOrder_redis == null ? 0 : userOrder_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中........");
                    sync = false;
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userOrder).put(order.getUserId(), userOrder_mysql);
                count ++;

            }

        }

        if(count != 0) {
            log.info("开始同步到Redis中........共同步了：" + count + "条数据！");
        }
        return sync;
    }


    @Override
    public boolean check_tb_goods(){
        log.info("检查db_goods库（tb_sku表开始）........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_goods).keys();
        List<Goods> goodsList = goodsMapper.selectAllGoods();
        boolean sync = true;
        if(keys.size() != goodsList.size()){
            log.info("开始同步到Redis中........");
            redisTemplate.delete(SystemConstants.redis_goods);
            int count = 0;
            for(Goods good : goodsList){
                redisTemplate.boundHashOps(SystemConstants.redis_goods).put(good.getId(),good);
                count ++;
            }
            sync = false;
            log.info("开始同步到Redis中........共同步了：" + count + "条数据！");
        }
        return sync;
    }
}
