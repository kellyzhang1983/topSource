package com.zkcompany.task;


import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Order;
import com.zkcompany.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

@Slf4j
@Component
public class OrderTimeTask {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 0/1 * * * ?")
    @GlobalTransactional
    public void closeOrder(){
        log.info("开始扫描订单......");
        int count = 0 ;
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_Order).keys();
        Iterator orderIterator = keys.iterator();
        while(orderIterator.hasNext()){
            Order order = (Order)redisTemplate.boundHashOps(SystemConstants.redis_Order).get(orderIterator.next());
            //状态为1、未付款的定案才能关闭
            if(order.getOrderState().equals("1")){
                //判断是否是普通订单，普通订单走定时任务关闭
                if(StringUtils.isEmpty(order.getOrderActivity())){
                    Date orderDate = order.getOrderDate();
                    LocalDateTime orderDateTime = orderDate.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
                    Duration duration = Duration.between(orderDateTime, LocalDateTime.now());
                    if(duration.toHours() >= 1){
                        try {
                            orderService.closeOrder(order.getId());
                            count ++ ;
                        } catch (Exception e) {
                            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【orderServerError：OrderTimeTask】orderService.closeOrder调取方法报错！订单编号：" + order.getId());
                        }

                       /* //2.回退商品库存
                        try {
                            orderService.fallbackGoodNum(order);
                        } catch (Exception e) {
                            throw new RuntimeException("调用fallbackGoodNum回退商品库存失败！请查看详细原因：" + e.getMessage());
                        }

                        //3.回退商品销量
                        try {
                            orderService.fallbackGoodSaleNum(order);
                        } catch (Exception e) {
                            throw new RuntimeException("调用fallbackGoodSaleNum回退商品销量失败！请查看详细原因：" + e.getMessage());
                        }*/
                    }
                }

            }
        }
        log.info("关闭订单数量：" + count);
        log.info("扫描订单结束......");
    }
}
