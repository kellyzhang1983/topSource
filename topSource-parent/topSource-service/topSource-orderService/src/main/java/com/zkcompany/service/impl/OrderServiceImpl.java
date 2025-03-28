package com.zkcompany.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.zkcompany.dao.OrderMapper;
import com.zkcompany.entity.*;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.User;
import com.zkcompany.rocketmq.SendMessageMQ;
import com.zkcompany.service.OrderService;
import io.github.resilience4j.retry.annotation.Retry;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService, UserDetailsService {

    @Autowired
    private SendMessageMQ sendMessageMQ;

    @Autowired
    private UserCenterFegin userCenterFegin;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdWorker idCreate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Result<User> searchUser(String id) throws Exception {
        return userCenterFegin.findUser(id);
    }

    @Override
    public Result<User> addUserPoint(String user_id) throws Exception {
        Order order = createOrder(user_id);
        Map<String, Object> point = create_point(order);
        Result result = userCenterFegin.addUserPoint(point);
        return result;
    }

    @Override
    @GlobalTransactional
    @Deprecated
    public int paySatus(Order order) throws Exception {
        //根据条件查询订单，先从Redis查询，再从MySQL查询（示例直接从MySQL查询）
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",order.getId());
        criteria.andEqualTo("user_id",order.getUser_id());
        Order order_user = orderMapper.selectOneByExample(example);
        //首先判断有没有订单记录，如果没有返回0；
        if(Objects.isNull(order_user)){
            return 0;
        }

        switch (order_user.getOrder_state()){
            case "2": //如果订单状态等于2，那么直接返回2供前端做判断
                return 2;
            case "3": //如果订单状态等于3，那么直接返回3供前端做判断
                return 3;
            default:
                //如果订单状态等于1，那么直接将订单状态改变成2，并增加500积分
                order_user.setOrder_state("2");
                orderMapper.updateByPrimaryKeySelective(order_user);

                Map<String,Object> body = new HashMap<String,Object>();
                body.put("user_id",order_user.getUser_id());
                body.put("change_type",1);
                body.put("points_detail",order_user.getId());
                body.put("point",500);

                //调用fegin接口，增加积分；
                Result result = userCenterFegin.addUserPoint(body);
                if(!result.isFlag()){
                    throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                }
                return 1;
        }
    }

    @Override
    @GlobalTransactional
    @Deprecated
    public int cancelOrder(Order order) {
        Order order_his = orderMapper.selectByPrimaryKey(order);
        if(order_his == null) {
            return 0;
        }else if(order_his.getOrder_state().equals("2")){
            return 1;
        }else {
            order.setOrder_state("3");
            int i = orderMapper.updateByPrimaryKeySelective(order);
            if(i > 0){
                Result result = userCenterFegin.cancelPoint(order.getUser_id(),order.getId());
                if(!result.isFlag()){
                    log.info("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                    throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" );
                }
            }
            return 2;
        }
    }

    @Override
    @GlobalTransactional
    @Deprecated
    public int cretaOrder(Order order) {
        int reuslt = orderMapper.insertSelective(order);
        Map<String, Object> point = create_point(order);

        if(reuslt > 0){
            Result result = userCenterFegin.addUserPoint(point);
            if(!result.isFlag()){
                throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
            }
        }
        JSONObject josn_order = (JSONObject)JSONObject.toJSON(order);
        sendMessageMQ.SendMessage_async_delay(order,RocketMQInfo.rocketMQ_topic_orderProcessDelay,3000,9,josn_order);
        return reuslt;
    }

    @Override
    public String placeOrder(String user_id) throws Exception {
        Order order = createOrder(user_id);
        JSONObject josn_order = (JSONObject)JSONObject.toJSON(order);
        //sendMessageMQ.SendMessage_async(order,RocketMQInfo.rocketMQ_topic_orderProcess,josn_order);
        sendMessageMQ.SendMessage_async(order,RocketMQInfo.rocketMQ_topic_orderProcess,josn_order);
        return order.getId();
    }

    @Override
    public Order searchOrder(String order_id) throws Exception {
        Order order = redisTemplate.boundHashOps(SystemConstants.redis_Order).get(order_id) == null ? null : (Order) redisTemplate.boundHashOps(SystemConstants.redis_Order).get(order_id);
        if(Objects.isNull(order)){
            Order orderByKey = new Order();
            orderByKey.setId(order_id);
            order = orderMapper.selectByPrimaryKey(orderByKey);
        }
        return order;
    }

    private Map<String,Object> create_point(Order order){
        Map<String,Object> body = new HashMap<String,Object>();
        body.put("user_id",order.getUser_id());
        body.put("change_type",3);
        body.put("points_detail",order.getId());
        body.put("point",200);
        return body;
    }

    private Order createOrder(String user_id){
        Order order = new Order();
        order.setId(String.valueOf(idCreate.nextId()));
        order.setUser_id(user_id);
        // 定义订单金额的最小值和最大值
        BigDecimal min = new BigDecimal("10");
        BigDecimal max = new BigDecimal("500");

        // 生成随机金额
        BigDecimal randomAmount = generateRandomAmount(min, max);
        order.setOrder_money(randomAmount);
        order.setOrder_state("1");
        order.setOrder_date(WorldTime.chinese_time(new Date()));

        //自动生成订单，需要获取一个用户认证和权限，系统内置一个认证用户（system_user）和权限(所有权限)

        return order;
    }

    private BigDecimal generateRandomAmount(BigDecimal min, BigDecimal max) {
        // 生成一个 0 到 1 之间的随机小数
        double randomDouble = ThreadLocalRandom.current().nextDouble();
        // 计算差值
        BigDecimal range = max.subtract(min);
        // 计算随机金额
        BigDecimal randomValue = new BigDecimal(randomDouble).multiply(range).add(min);
        // 保留两位小数
        return randomValue.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

}
