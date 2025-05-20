package com.zkcompany.task;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.fegin.GoodsCenterFegin;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.Goods;
import com.zkcompany.pojo.MarketActivity;
import com.zkcompany.service.MarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class MarketActivityTimerTask {

    @Autowired
    private MarketService marketService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodsCenterFegin goodsCenterFegin;

    @Scheduled(cron = "0 0/1 * * * ?")
    @GlobalTransactional
    public void beginAndEndMarketActivity(){
        log.info("开始扫描活动......");
        int beginMarketActivity = 0;
        int endMarketActivity = 0;
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).keys();
        Iterator marketIterator = keys.iterator();
        while(marketIterator.hasNext()){
            MarketActivity marketActivity = (MarketActivity)redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).get(marketIterator.next());
            Date beginDate = marketActivity.getBeginDate();
            Date endDate = marketActivity.getEndDate();
            LocalDateTime beginDateTime = beginDate.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
            LocalDateTime endDateTime = endDate.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();

            if(marketActivity.getActivityStatus().equals("0")){
                if(now.isAfter(beginDateTime)){
                    beginMarketActivity ++;
                    marketActivity.setActivityStatus("1");
                    try {
                        marketService.updateMarketActivityStatus(marketActivity);
                    } catch (Exception e) {
                        throw new RuntimeException("【MarketActivityTimerTask：beginAndEndMarketActivity】marketService.updateMarketActivityStatus调取方法报错！活动编号：" + marketActivity.getId() + "error:" + e.getMessage());
                    }
                    List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(marketActivity.getId());
                    for(ActivityGoods activityGoods : activityGoodsList){
                        try{
                            Integer [] goodsNums = new Integer[activityGoods.getGoodsNum()];
                            for(int i = 0; i < activityGoods.getGoodsNum(); i++){
                                goodsNums[i] = 1;
                            }
                            redisTemplate.boundListOps(SystemConstants.redis_activityGoodsNum + "_" + activityGoods.getMarketId() + "_" + activityGoods.getGoodsId()).leftPushAll(goodsNums);
                        } catch (Exception e) {
                            throw new RuntimeException("【MarketActivityTimerTask：beginAndEndMarketActivity】redisTemplate.boundListOps failed! error:  " + e.getMessage());
                        }
                    }

                }
            }
            if(marketActivity.getActivityStatus().equals("1")){
                if(now.isAfter(endDateTime)){
                    endMarketActivity++;
                    marketActivity.setActivityStatus("2");
                    try {
                        marketService.updateMarketActivityStatus(marketActivity);
                    } catch (Exception e) {
                        throw new RuntimeException("【MarketActivityTimerTask：beginAndEndMarketActivity】营销活动关闭失败 failed!！活动编号：" + marketActivity.getId() + "error:" + e.getMessage());
                    }

                    List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(marketActivity.getId());
                    for(ActivityGoods activityGoods : activityGoodsList){
                        try {
                            BoundListOperations boundListOperations = redisTemplate.boundListOps(SystemConstants.redis_activityGoodsNum + "_" + activityGoods.getMarketId() + "_" + activityGoods.getGoodsId());
                            if(!ObjectUtils.isEmpty(boundListOperations)){
                                Long activityGoodsNumSize = boundListOperations.size();
                                Goods goods = new Goods();
                                goods.setId(activityGoods.getGoodsId());
                                goods.setNum(Integer.valueOf(activityGoodsNumSize.toString()));
                                Result result = goodsCenterFegin.updateGoodsNumInventoryTimeTask(goods, "add");
                                if(!result.isFlag()){
                                    throw new RuntimeException("【MarketActivityTimerTask：beginAndEndMarketActivity】goodsCenterFegin.updateGoodsNumInventoryTimeTask远程调用失败 failed! : ");
                                }
                            }
                            redisTemplate.delete(SystemConstants.redis_activityGoodsNum + "_" + activityGoods.getMarketId() + "_" + activityGoods.getGoodsId());
                        } catch (Exception e) {
                            throw new RuntimeException("【MarketActivityTimerTask：beginAndEndMarketActivity】营销活动关闭后回退商品库存失败 failed!  error: " + e.getMessage());
                        }
                    }
                }
            }

        }
        log.info("现在开始的营销活动总共有：" + beginMarketActivity + "个");
        log.info("现在结束的营销活动总共有：" + endMarketActivity + "个");
        log.info("扫描活动结束......");
    }
}
