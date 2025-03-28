package com.zkcompany.task;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zkcompany.entity.*;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.User;
import com.zkcompany.rocketmq.SendMessageMQ;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class OrderCreateSendMQ {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SendMessageMQ sendMessageMQ;

    @Autowired
    private UserCenterFegin userCenterFegin;

    @Autowired
    private IdWorker idCreate;
    private Set user_keys;
    private int redis_index = 0;

    private int mysql_index = 0;

    private  Result<Map<String, Object>> allUserPage;

    private  Map<String, Object> userMap;

    private List<User> userList;

    public static String token;




    //@Scheduled(cron = "0/10 * * * * ?")
    @CircuitBreaker(name = "order_orderServcie_orderProcess", fallbackMethod = "serviceError")
    public void createOrderSendMQTask(){
        //System.out.println("每5秒执行一次的任务");
        if(token == null || "".equals(token)){
            token = authorizationUser();
        }

        //log.info("每10秒执行一次的任务");
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
            //Order order = createOrder(String.valueOf("1881706924655902720"));
            JSONObject josn_order = (JSONObject)JSONObject.toJSON(order);
            sendMessageMQ.SendMessage_async(order,RocketMQInfo.rocketMQ_topic_orderProcess,josn_order);

            if(user_keys.size() -1 == redis_index){
                redis_index = 0;
            }else{
                redis_index++;
            }
        }else {
            if(mysql_index == 0){
                requestAuth();
                allUserPage = userCenterFegin.findAllUserPage();
                userMap = (Map<String, Object>) allUserPage.getData();
                userList = (List<User>) userMap.get("DataList");
                //System.out.println(String.format("第一次从mysql中读取数据，下标：%s的时候调取远程方法......",mysql_index));
                //log.info(String.format("第一次从mysql中读取数据，下标：%s的时候调取远程方法......",mysql_index));
            }

            if (!(userList == null || userList.isEmpty())){
                Map userMap = (Map) userList.get(mysql_index);
                Order order = createOrder(userMap.get("id").toString());

                JSONObject josn_order = (JSONObject)JSONObject.toJSON(order);
                sendMessageMQ.SendMessage_async(order,RocketMQInfo.rocketMQ_topic_orderProcess,josn_order);
                //System.out.println(String.format("从mysql中读取数据完成，user_id:%s、下标：%s,List中共有：%s条数据",userMap.get("id"),mysql_index,userList.size()));
                //log.info(String.format("从mysql中读取数据完成，user_id:%s、下标：%s,List中共有：%s条数据",userMap.get("id"),mysql_index,userList.size()));
                if(userList.size() -1 == mysql_index) {
                    mysql_index = 0;
                }else {
                    mysql_index++;
                }
            }
        }
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

    public void serviceError(Throwable e){
        //System.out.println(StatusCode.REMOTEERROR + "message：CircuitBreaker：服务器开小差了！，请稍后再试....." + e.getMessage());
        log.error("错误码：" +StatusCode.REMOTEERROR + "message：CircuitBreaker：服务器开小差了！，请稍后再试....." + e.getMessage());
    }

    private String authorizationUser() throws RuntimeException {
        /*MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        MultiValueMap<String,String> formData = new LinkedMultiValueMap<>();
        formData.add("username","kelly1");
        formData.add("password","123456");
        HttpEntity<MultiValueMap> requestentity = new HttpEntity<MultiValueMap>(formData,headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange("http://localhost:9091/userAuthentication/login", HttpMethod.POST, requestentity, Map.class);

        Map responseEntityBody = responseEntity.getBody();
        JSONObject data = (JSONObject)responseEntityBody.get("data");*/
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:9091/userAuthentication/login");
        // 设置请求头
        httpPost.setHeader("Content-Type", "application/json");

        // 设置请求体
        String jsonBody = "{\"username\": \"kelly1\",\"password\":\"123456\"}";
        String token = "";
        try {
            StringEntity entity = new StringEntity(jsonBody);
            httpPost.setEntity(entity);
            // 发送请求并获取响应
            HttpResponse response = httpClient.execute(httpPost);

            // 获取响应内容
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response Body: " + responseBody);

            JSONObject parseObject = JSON.parseObject(responseBody);
            Map data = (Map)parseObject.get("data");

            token = data.get("token").toString();


        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    private void requestAuth(){
        String token = OrderCreateSendMQ.token;
        // 手动创建 HttpServletRequest 和 HttpServletResponse 模拟对象
        HttpServletRequest request_token = new com.zkcompany.entity.HttpServletRequest();
        HttpServletResponse response_token = new com.zkcompany.entity.HttpServletResponse();
        ((com.zkcompany.entity.HttpServletRequest) request_token).addHeader("Authorization",token);
        ServletRequestAttributes attributes = new ServletRequestAttributes(request_token, response_token);

        // 将请求上下文设置到 RequestContextHolder 中
        RequestContextHolder.setRequestAttributes(attributes);
    }

}
