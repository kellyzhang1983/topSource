package com.zkcompany.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.dao.ActivityGoodsMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.OrderGoods;
import com.zkcompany.service.ProcessActivityGoods;
import com.zkcompany.uitl.ConvertObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SynActivityGoods implements ProcessActivityGoods {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ActivityGoodsMapper activityGoodsMapper;
    @Override
    public int activityGoods_addOrUpdateRedis(List<CanalEntry.Column> columns, Map<String,String> activityGoodsMap, Integer count) {
        ActivityGoods activityGoods = new ActivityGoods();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            switch (name) {
                case "market_id":
                    ConvertObject.convertActivityGoods(activityGoods, name, column.getValue());
                    String activityGoodsId = activityGoodsMap.get(column.getValue());
                    if (StringUtils.isEmpty(activityGoodsId)) {
                        //查询数据库，把所有Order_id想同的数据全部查询出来，返回一个 List<OrderGoods>
                        List<ActivityGoods> activityGoodsList = activityGoodsMapper.selectMarketActivityGoods(column.getValue());
                        try {
                            //放入Redis中，数据格式为Map<order_id,List<OrderGoods>
                            redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).put(column.getValue(), activityGoodsList);
                            //记录该用户已从数据库通过order_id查询出已修改所有的商品信息，当再查询的时候不予执行；
                            activityGoodsMap.put(column.getValue(), "1");
                            count = count + 1;
                        } catch (Exception e) {
                            // 处理异常，例如记录日志或采取其他措施
                            log.error("Redis operation activityGoods_addOrUpdateRedis failed: " + e.getMessage());
                        }
                    }
                    break;
                default:
                    ConvertObject.convertActivityGoods(activityGoods, name, column.getValue());
                    break;
            }
        }
        /*Integer [] goodsNums = new Integer[activityGoods.getGoodsNum()];
        for(int i = 0; i < activityGoods.getGoodsNum(); i++){
            goodsNums[i] = 1;
        }
        try{
            redisTemplate.boundListOps(SystemConstants.redis_activityGoodsNum + "_" + activityGoods.getMarketId() + "_" + activityGoods.getGoodsId()).leftPushAll(goodsNums);
        } catch (Exception e) {
            throw new RuntimeException("Redis operation(add) redis_activityGoodsNum failed: " + e.getMessage());
        }*/

        return count;
    }

    @Override
    public void activityGoods_deleteRedis(List<CanalEntry.Column> columns) {
        String market_id = "";
        String id = "";
        String goodsId = "";

        for (CanalEntry.Column column : columns){
            switch (column.getName()){
                case "market_id":
                    market_id = column.getValue();
                    break;
                case "id":
                    id = column.getValue();
                    break;
                case "goods_id":
                    goodsId = column.getValue();
                    break;
            }

            if(!(StringUtils.isEmpty(market_id) || StringUtils.isEmpty(id) || StringUtils.isEmpty(goodsId))){
                break;
            }
        }
        try {
            redisTemplate.delete(SystemConstants.redis_activityGoodsNum + "_" + market_id + "_" + goodsId);
        } catch (Exception e) {
            log.error("Redis operation activityGoods_deleteRedis failed: " + e.getMessage());
        }
        /*try {
            redisTemplate.boundHashOps(SystemConstants.redis_goodsActivity).delete(id);
        } catch (Exception e) {
            log.error("Redis operation activityGoods_deleteRedis failed: " + e.getMessage());
        }*/

        List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(market_id);
        for(ActivityGoods activityGoods : activityGoodsList){
            if(goodsId.equals(activityGoods.getGoodsId())){
                activityGoodsList.remove(activityGoods) ;
                try {
                    redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).put(market_id,activityGoodsList);
                } catch (Exception e) {
                    log.error("Redis operation activityGoods_deleteRedis failed: " + e.getMessage());
                }
                break;
            }
        }
    }
}
