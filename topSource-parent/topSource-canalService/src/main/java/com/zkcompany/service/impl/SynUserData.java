package com.zkcompany.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.dao.UserPointMapper;
import com.zkcompany.dao.UserRoleMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Point;
import com.zkcompany.pojo.User;
import com.zkcompany.pojo.UserRole;
import com.zkcompany.service.ProcessUserData;
import com.zkcompany.uitl.ConvertObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class SynUserData implements ProcessUserData {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserPointMapper userPointMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public void user_addOrUpdateRedis(List<CanalEntry.Column> columns) {
        User user = new User();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            //得到列的值
            String value = column.getValue();
            //转换成USER对象
            ConvertObject.convertUser(user,name,value);
        }
        try {
            //数据格式为：Redis<key>,{user_id = ?;user对象}
            redisTemplate.boundHashOps(SystemConstants.redis_userInfo).put(user.getId(),user);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            System.err.println("Redis operation addOrUpdateRedis  failed: " + e.getMessage());
        }
    }

    @Override
    public void user_deleteRedis(List<CanalEntry.Column> columns){
        String id = "";

        for (CanalEntry.Column column : columns){
            if(column.getName().equals("id")){
                id = column.getValue();
                break;
            }
        }
        try {
            redisTemplate.boundHashOps(SystemConstants.redis_userInfo).delete(id);
            redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).delete(id);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            System.err.println("Redis operation deleteRedis failed: " + e.getMessage());
        }
    }

    @Override
    public Integer point_addOrUpdateRedis(List<CanalEntry.Column> columns, Map<String,String> userMap,Integer count) {
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            if(name.equals("user_id")){
                String userid = userMap.get(column.getValue());
                //查询数据库，把所有user_id想同的数据全部查询出来，返回一个List<Point>
                /**
                 * 例如，删除10条数据，其中有三条数据的user_id是一样的，那么那转换成MAP<user_id,List<point>>的数据结构
                 * 1、先从数据库查询出来user_id相关联的数据（1对多），放入List集合中。
                 * 2、再把第一步查询出来的List放入Redis中。形成一个新的MAP<user_id,List<point>>
                 * 3、然后对user_id做上userMap.put(column.getValue(),"1")标示;
                 * 4、如果再有同样的user_id,数据已经是最新的了，不需要再从数据库同步，避免重复去查数据库，造成资源浪费
                 * **/
                if(StringUtils.isEmpty(userid)){
                    List<Point> points = userPointMapper.searchUserPoint(column.getValue());
                    try {
                        //放入Redis中，数据格式为Map<user_id,List<Point>>
                        redisTemplate.boundHashOps(SystemConstants.redis_userPoint).put(column.getValue(),points);
                        userMap.put(column.getValue(),"1");
                        count = count + 1 ;
                    } catch (Exception e) {
                        // 处理异常，例如记录日志或采取其他措施
                        System.err.println("Redis operation redis_userPoint failed: " + e.getMessage());
                    }
                    break;
                }
            }
        }
        return count;
    }

    @Override
    public Integer point_deleteRedis(List<CanalEntry.Column> columns, Map<String, String> userMap, Integer count) {
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            if(name.equals("user_id")){
                String userid = userMap.get(column.getValue());
                /**
                 * 例如，删除10条数据，其中有三条数据的user_id是一样的，那么那转换成MAP<user_id,List<point>>的数据结构
                 * 1、先从数据库查询出来user_id相关联的数据（1对多），放入List集合中。
                 * 2、如果集合为空，那证明已经没有数据跟user_id相关联，就直接删除掉Redis的key(user_id)
                 * 2、再把第一步查询出来的List放入Redis中。形成一个新的MAP<user_id,List<point>>
                 * 3、然后对user_id做上userMap.put(column.getValue(),"1")标示;
                 * 4、如果再有同样的user_id,数据已经是最新的了，不需要再从数据库同步，避免重复去查数据库，造成资源浪费
                 * **/
                if(StringUtils.isEmpty(userid)){
                    //查询数据库，把所有user_id想同的数据全部查询出来，返回一个List<Point>
                    List<Point> points = userPointMapper.searchUserPoint(column.getValue());
                    try {
                        if(Objects.isNull(points)){
                            redisTemplate.boundHashOps(SystemConstants.redis_userPoint).delete(column.getValue());
                        }else{
                            //放入Redis中，数据格式为Map<user_id,List<Point>>
                            redisTemplate.boundHashOps(SystemConstants.redis_userPoint).put(column.getValue(),points);
                        }

                        userMap.put(column.getValue(),"1");
                        count = count + 1 ;
                    } catch (Exception e) {
                        // 处理异常，例如记录日志或采取其他措施
                        System.err.println("Redis operation redis_userPoint failed: " + e.getMessage());
                    }
                    break;
                }
            }
        }
        return count;
    }

    @Override
    public Integer user_addRole(List<CanalEntry.Column> columns , Map<String,String> userMap,Integer count) {

        List<String> roleList = new ArrayList<>();;
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            if(name.equals("user_id")){
                String userid = userMap.get(column.getValue());
                if(StringUtils.isEmpty(userid)){
                    //查询数据库，把所有user_id所关联的权限和角色全部查询出来。
                    List<UserRole> userRoleList = userRoleMapper.selectRole(column.getValue());
                    try {
                        for(UserRole userRoleObject : userRoleList){
                            //角色前面加上前缀ROLE_,区分角色和权限
                                String role_name = "ROLE_" + userRoleObject.getRole_name();
                        roleList.add(role_name);
                    }
                    //放入Redis中，数据格式为Map<user_id,List<String>>
                    redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).put(column.getValue(),roleList);
                    } catch (Exception e) {
                        // 处理异常，例如记录日志或采取其他措施
                        System.err.println("Redis operation redis_userPoint failed: " + e.getMessage());
                    }
                    break;
                }
            }
        }
        return count;
    }

    @Override
    public void user_deleteRole(List<CanalEntry.Column> columns) {
        String user_id = "";

        for (CanalEntry.Column column : columns){
            switch (column.getName()){
                case "user_id":
                    user_id = column.getValue();
                    break;
            }
        }
        try {
            //通过role_id把role_name找到
            List<String> userRoles = userRoleMapper.selectUserRole(user_id);
            if(Objects.isNull(userRoles)){
                redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).delete(user_id);
            }else{
                userRoles.replaceAll(element -> "ROLE_" + element);
                redisTemplate.boundHashOps(SystemConstants.redis_userRoleAndPermission).put(user_id,userRoles);
            }

        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            System.err.println("Redis operation deleteRedis failed: " + e.getMessage());
        }
    }

}
