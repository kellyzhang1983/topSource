package com.zkcompany.task;

import com.zkcompany.dao.OrderMapper;
import com.zkcompany.entity.*;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//@Component
public class OrderCreateTask {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserCenterFegin userCenterFegin;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdWorker idCreate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private int redis_index = 0;

    private int mysql_index = 0;



    private  Result<Map<String, Object>> allUserPage;

    private  Map<String, Object> userMap;

    private List<User> userList;

    private Set user_keys;

    //@Scheduled(cron = "0 0/10 * * * ?")
    @CircuitBreaker(name = "order_orderServcie_findAllUser", fallbackMethod = "serviceError")
    //@GlobalTransactional
    public void createOrderTask(){
        System.out.println("每1分钟执行一次的任务");
        //从Redis读取数据，数据结构为Map<user_id,List<point>>......
        if(redis_index == 0){
            user_keys = redisTemplate.boundHashOps(SystemConstants.redis_userInfo).keys();
        }
        //如果Redis没有数据，那么直接从MySQL读取数据
        if (!(user_keys == null || user_keys.isEmpty())){
            //转换成LIST......
            Object[] user_ids = user_keys.toArray();
            //把user_id传进去创建订单，生产Order对象
            //redis_index:List[redis_index] : 每个用户循环生产订单
            Order order = createOrder(String.valueOf(user_ids[redis_index]));
            //插入数据库（订单表）
            orderMapper.insertSelective(order);
            //创建积分对象
            Map<String, Object> point = create_point(order);
            // 下单后增加积分200分，利用动态fegin（userCenterFegin）调取addUserPoint增加积分
            //userCenterFegin.addUserPoint(point);
            System.out.println(String.format("从Redis中读取数据完成，user_id:%s、Redis【Set】下标：%s,Set中共有：%s条数据",String.valueOf(user_ids[redis_index]),redis_index,user_keys.size()));
            //当redis_index 的大小 == list大小，又重新循环生产订单
            if(user_keys.size() -1 == redis_index){
                redis_index = 0;
            }else{
                redis_index++;
            }
        }else {
            if(mysql_index == 0){
                /*allUserPage = userCenterFegin.findAllUserPage();
                userMap = (Map<String, Object>) allUserPage.getData();
                userList = (List<User>) userMap.get("DataList");*/
                System.out.println(String.format("第一次从mysql中读取数据，下标：%s的时候调取远程方法......",mysql_index));
            }

            if (!(userList == null || userList.isEmpty())){
                Map userMap = (Map) userList.get(mysql_index);
                Order order = createOrder(userMap.get("id").toString());
                orderMapper.insertSelective(order);
                //创建积分对象
                Map<String, Object> point = create_point(order);
                // 下单后增加积分200分，利用动态fegin（userCenterFegin）调取addUserPoint增加积分
                //userCenterFegin.addUserPoint(point);
                System.out.println(String.format("从mysql中读取数据完成，user_id:%s、下标：%s,List中共有：%s条数据",userMap.get("id"),mysql_index,userList.size()));
                if(userList.size() -1 == mysql_index) {
                    mysql_index = 0;
                }else {
                    mysql_index++;
                }
            }
        }
    }

    private Order createOrder(String user_id){
        idCreate = new IdWorker();
        Order order = new Order();
        order.setId(String.valueOf(idCreate.nextId()));
        order.setUser_id(user_id);
        // 定义订单金额的最小值和最大值
        BigDecimal min = new BigDecimal("10");
        BigDecimal max = new BigDecimal("500");
        SendResult sendResult = rocketMQTemplate.syncSend("", "");
        SendStatus sendStatus = sendResult.getSendStatus();

        // 生成随机金额
        BigDecimal randomAmount = generateRandomAmount(min, max);
        order.setOrder_money(randomAmount);
        order.setOrder_state("1");
        order.setOrder_date(WorldTime.chinese_time(new Date()));

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

    public void serviceError(Throwable e){
        System.out.println(StatusCode.REMOTEERROR + "message：CircuitBreaker：服务器开小差了！，请稍后再试....." + e.getMessage() );
        throw new RuntimeException(e);
        //return new Result<>(false, StatusCode.REMOTEERROR,"CircuitBreaker：服务器开小差了！，请稍后再试.....",e.getMessage());
        //System.out.println(StatusCode.REMOTEERROR + "message：CircuitBreaker：服务器开小差了！，请稍后再试....." + e.getMessage() );
    }
}
