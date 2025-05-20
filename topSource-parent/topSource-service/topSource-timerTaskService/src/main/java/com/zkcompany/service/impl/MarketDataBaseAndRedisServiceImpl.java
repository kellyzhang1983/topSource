package com.zkcompany.service.impl;

import com.zkcompany.dao.ActivityGoodsMapper;
import com.zkcompany.dao.ActivityStatusMapper;
import com.zkcompany.dao.MarketActivityMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.ActivityStatus;
import com.zkcompany.pojo.MarketActivity;
import com.zkcompany.service.MarketDataBaseAndRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class MarketDataBaseAndRedisServiceImpl implements MarketDataBaseAndRedisService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ActivityGoodsMapper activityGoodsMapper;

    @Autowired
    private ActivityStatusMapper activityStatusMapper;
    @Autowired
    private MarketActivityMapper marketActivityMapper;
    @Override
    public boolean checkTbMarket() {
        log.info(".........检查db_market库（tb_market表）开始.........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).keys();
        List<MarketActivity> marketActivityList = marketActivityMapper.selectMarketActivity();
        boolean sync = true;
        if(keys.size() != marketActivityList.size()){
            log.info("开始同步到Redis中！<<<<<<<<<");
            redisTemplate.delete(SystemConstants.redis_marketActivity);
            int count = 0;
            for(MarketActivity marketActivity : marketActivityList){
                redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).put(marketActivity.getId(),marketActivity);
                count ++;
            }
            sync = false;
            log.info("数据同步到Redis（redis_marketActivity），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_order库（tb_market表）结束.........");
        return sync;
    }

    @Override
    public boolean checkTbMarketGoods() {
        log.info(".........检查db_market库（tb_activity_goods表）开始........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).keys();
        List<MarketActivity> marketActivityList = marketActivityMapper.selectMarketActivity();
        boolean sync = true;
        int count = 0;
        if(keys.size() != marketActivityList.size()){
            log.info("开始删除Redis中的redis_marketActivityGoods数据<<<<<<<<<");
            sync = false;
            for (Object key : keys) {
                Long delete = redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).delete(key);
                count = count + Integer.valueOf(delete.toString());
            }
            log.info("删除Redis中的redis_marketActivityGoods数据：" + count + "条数据！<<<<<<<<<");
        }
        count = 0;
        for(MarketActivity marketActivity : marketActivityList){
            List<ActivityGoods> activityGoodsList_mysql = activityGoodsMapper.selectMarketActivityGoods(marketActivity.getId());
            List<ActivityGoods> activityGoodsList_redis = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(marketActivity.getId());
            if(activityGoodsList_mysql.size() != (activityGoodsList_redis == null ? 0 : activityGoodsList_redis.size())){
                if(count == 0){
                    log.info("开始同步到Redis中！<<<<<<<<<");
                    sync = false;
                }
                redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).put(marketActivity.getId(), activityGoodsList_mysql);
                count ++;

            }

        }

        if(count != 0) {
            log.info("数据同步到Redis（redis_marketActivityGoods），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_market库（tb_activity_goods表）结束.........");
        return sync;
    }

    @Override
    public boolean checkTbUserActivityStatus() {
        log.info(".........检查db_market库（tb_activity_status表）开始........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).keys();
        List<String> activityStatusList = activityStatusMapper.selectActivityStatusGroupUser();
        boolean sync = true;
        int count = 0;
        if(keys.size() != activityStatusList.size()){
            log.info("开始删除Redis中的redis_userActivityStatus数据<<<<<<<<<");
            sync = false;
            for (Object key : keys) {
                Long delete = redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).delete(key);
                count = count + Integer.valueOf(delete.toString());
            }
            log.info("删除Redis中的redis_userActivityStatus数据：" + count + "条数据！<<<<<<<<<");
        }

        count = 0;
        for(String userId : activityStatusList) {
            List<ActivityStatus> activityStatusList_mysql = activityStatusMapper.selectActivityStatusByUserid(userId);
            List<ActivityStatus> activityStatusList_redis = (List<ActivityStatus>) redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).get(userId);
            if (activityStatusList_mysql.size() != (activityStatusList_redis == null ? 0 : activityStatusList_redis.size())) {
                if (count == 0) {
                    log.info("开始同步到Redis中！<<<<<<<<<");
                    sync = false;
                }
                redisTemplate.boundHashOps(SystemConstants.redis_userActivityStatus).put(userId, activityStatusList_mysql);
                count++;

            }
        }

        if(count != 0) {
            log.info("数据同步到Redis（redis_userActivityStatus），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_market库（tb_activity_status表）结束.........");
        return sync;
    }

    @Override
    public boolean checkTbActivityStatus() {
        log.info(".........检查db_market库（tb_activity_status表）开始.........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_activityStatus).keys();
        List<ActivityStatus> activityStatusList = activityStatusMapper.selectActivityStatus();
        boolean sync = true;
        if(keys.size() != activityStatusList.size()){
            log.info("开始同步到Redis中！<<<<<<<<<");
            redisTemplate.delete(SystemConstants.redis_activityStatus);
            int count = 0;
            for(ActivityStatus activityStatus : activityStatusList){
                redisTemplate.boundHashOps(SystemConstants.redis_activityStatus).put(activityStatus.getOrderId(),activityStatus);
                count ++;
            }
            sync = false;
            log.info("数据同步到Redis（redis_activityStatus），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_market库（tb_activity_status表）结束.........");
        return sync;
    }
}
