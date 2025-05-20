package com.zkcompany.task;

import com.zkcompany.service.GoodsDataBaseAndRedisService;
import com.zkcompany.service.MarketDataBaseAndRedisService;
import com.zkcompany.service.OrderDataBaseAndRedisService;
import com.zkcompany.service.UserDataBaseAndRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckMySqlToRedis {

    @Autowired
    private UserDataBaseAndRedisService userDataBaseAndRedisService;

    @Autowired
    private OrderDataBaseAndRedisService orderDataBaseAndRedisService;

    @Autowired
    private GoodsDataBaseAndRedisService goodsDataBaseAndRedisService;

    @Autowired
    private MarketDataBaseAndRedisService marketDataBaseAndRedisService;

    @Scheduled(cron = "0 30 23 * * ?")
    public void checkTbUserTimerTask(){
        boolean syncTbUser = userDataBaseAndRedisService.checkTbUserToRedis();
        if(!syncTbUser){
            log.info("=========结论: Mysql(tb_user)数据库与Redis(redis_userInfo)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_user)数据库与Redis(redis_userInfo)数据一致，不需要同步=========");
        }

        boolean syncTbUserPoint = userDataBaseAndRedisService.checkTbUserPoint();
        if(!syncTbUserPoint){
            log.info("=========结论: Mysql(tb_user_points)数据库与Redis(redis_userPoint)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_user_points)数据库与Redis(redis_userPoint)数据一致，不需要同步=========");
        }

        boolean syncTbUserRole = userDataBaseAndRedisService.checkTbUserRole();
        if(!syncTbUserRole){
            log.info("=========结论: Mysql(tb_user_role)数据库与Redis(redis_userRoleAndPermission)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_user_role)数据库与Redis(redis_userRoleAndPermission)数据一致，不需要同步=========");
        }
    }

    @Scheduled(cron = "0 05 00 * * ?")
    public void checkTbOderTimerTask(){
        boolean syncTbOrder = orderDataBaseAndRedisService.checkTbOrder();
        if(!syncTbOrder){
            log.info("=========结论: Mysql(tb_order)数据库与Redis(redis_Order)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_order)数据库与Redis(redis_Order)数据一致，不需要同步=========");
        }

        boolean syncTbOrderGoods = orderDataBaseAndRedisService.checkTbOrderGoods();
        if(!syncTbOrderGoods){
            log.info("=========结论: Mysql(tb_order_goods)数据库与Redis(redis_orderGoods)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_order_goods)数据库与Redis(redis_orderGoods)数据一致，不需要同步=========");
        }

        boolean syncTbUserOrder = orderDataBaseAndRedisService.checkTbUserOrder();
        if(!syncTbUserOrder){
            log.info("=========结论: Mysql(tb_order)数据库与Redis(redis_userOrder)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_order)数据库与Redis(redis_userOrder)数据一致，不需要同步=========");
        }

    }

    @Scheduled(cron = "0 45 23 * * ?")
    public void checkTbGoodsTimerTask(){
        boolean syncTbGoods = goodsDataBaseAndRedisService.checkTbGoods();
        if(!syncTbGoods){
            log.info("=========结论: Mysql(tb_sku)数据库与Redis(redis_goods)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_sku)数据库与Redis(redis_goods)数据一致，不需要同步=========");
        }
    }

    @Scheduled(cron = "0 55 23 * * ?")
    public void checkTbMarketActivityTimerTask() {
        boolean syncTbMarketActivity = marketDataBaseAndRedisService.checkTbMarket();
        if(!syncTbMarketActivity){
            log.info("=========结论: Mysql(tb_marketActivity)数据库与Redis(redis_marketActivity)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_marketActivity)数据库与Redis(redis_marketActivity)数据一致，不需要同步=========");
        }

        boolean syncTbActivityGoods = marketDataBaseAndRedisService.checkTbMarketGoods();
        if(!syncTbActivityGoods){
            log.info("=========结论: Mysql(tb_activity_goods)数据库与Redis(redis_marketActivityGoods)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_activity_goods)数据库与Redis(redis_marketActivityGoods)数据一致，不需要同步=========");
        }

        boolean syncTbUserActivityStatus = marketDataBaseAndRedisService.checkTbUserActivityStatus();
        if(!syncTbUserActivityStatus){
            log.info("=========结论: Mysql(tb_activity_status)数据库与Redis(redis_userActivityStatus)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_activity_status)数据库与Redis(redis_userActivityStatus)数据一致，不需要同步=========");
        }

        boolean syncTbActivityStatus = marketDataBaseAndRedisService.checkTbActivityStatus();
        if(!syncTbActivityStatus){
            log.info("=========结论: Mysql(tb_activity_status)数据库与Redis(redis_activityStatus)数据不一致，需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_activity_status)数据库与Redis(redis_activityStatus)数据一致，不需要同步=========");
        }

    }
}
