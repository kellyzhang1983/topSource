package com.zkcompany.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.dao.ActivityGoodsMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.MarketActivity;
import com.zkcompany.pojo.Order;
import com.zkcompany.service.ProcessMarketActivity;
import com.zkcompany.uitl.ConvertObject;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SynMarketActivityData implements ProcessMarketActivity {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ActivityGoodsMapper activityGoodsMapper;

    @Autowired
    private MinioClient minioClient;

    @Override
    public void marketActivity_addOrUpdateRedis(List<CanalEntry.Column> columns) {
        MarketActivity marketActivity = new MarketActivity();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            switch (name){
                case "id":
                    ConvertObject.convertMarketActivity(marketActivity,name,column.getValue());
                    //查询数据库，把所有market_id相同的数据全部查询出来，返回一个List<Order>
                    List<ActivityGoods> activityGoodsList = activityGoodsMapper.selectMarketActivityGoods(column.getValue());
                    try {
                        //放入Redis中，数据格式为Map<market_id,List<ActivityGoodsMapper>>
                        redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).put(column.getValue(),activityGoodsList);
                    } catch (Exception e) {
                        // 处理异常，例如记录日志或采取其他措施
                        log.error("Redis operation marketActivity_addOrUpdateRedis failed: " + e.getMessage());
                    }
                    break;
                default:
                    ConvertObject.convertMarketActivity(marketActivity,name,column.getValue());
                    break;
            }
        }
        try {
            redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).put(marketActivity.getId(),marketActivity);
        } catch (Exception e) {
            log.error("Redis operation marketActivity_addOrUpdateRedis failed: " + e.getMessage());
        }



    }

    @Override
    public void marketActivity_deleteRedis(List<CanalEntry.Column> columns) {
        String id = "";

        for (CanalEntry.Column column : columns){
            switch (column.getName()){
                case "id":
                    id = column.getValue();
                    break;
            }
        }

        MarketActivity marketActivity = (MarketActivity)redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).get(id);
        if(!StringUtils.isEmpty(marketActivity.getActivityImage())){
            String activityImage = marketActivity.getActivityImage();
            int beginIndexOf = activityImage.lastIndexOf("/") + 1;
            int lastIndexOf = activityImage.lastIndexOf("?");
            String image = activityImage.substring(beginIndexOf, lastIndexOf);

            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket("topsource-marketactivity")
                        .object(image)
                        .build());
            } catch (Exception e) {
                log.error("Minio operation marketActivity_deleteMinio failed: " + e.getMessage());
            }
        }

        List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(id);
        for(ActivityGoods activityGoods : activityGoodsList){
            try {
                redisTemplate.delete(SystemConstants.redis_activityGoodsNum + "_" + activityGoods.getMarketId() + "_" + activityGoods.getGoodsId());
            } catch (Exception e) {
                log.error("Redis operation marketActivity_addOrUpdateRedis failed: " + e.getMessage());
            }
        }

        try {
            redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).delete(id);
        } catch (Exception e) {
            log.error("Redis operation marketActivity_deleteRedis failed: " + e.getMessage());
        }

        try {
            redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).delete(id);
        } catch (Exception e) {
            log.error("Redis operation marketActivity_deleteRedis failed: " + e.getMessage());
        }
    }
}
