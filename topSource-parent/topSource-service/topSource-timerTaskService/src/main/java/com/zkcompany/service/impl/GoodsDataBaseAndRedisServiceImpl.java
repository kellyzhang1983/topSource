package com.zkcompany.service.impl;

import com.zkcompany.dao.GoodsMapper;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Goods;
import com.zkcompany.service.GoodsDataBaseAndRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
@Slf4j
@Component
public class GoodsDataBaseAndRedisServiceImpl implements GoodsDataBaseAndRedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodsMapper goodsMapper;
    @Override
    public boolean checkTbGoods() {
        log.info(".........检查db_goods库（tb_sku表）开始.........");
        Set keys = redisTemplate.boundHashOps(SystemConstants.redis_goods).keys();
        List<Goods> goodsList = goodsMapper.selectAllGoods();
        boolean sync = true;
        if(keys.size() != goodsList.size()){
            log.info("开始同步到Redis中!<<<<<<<<<");
            redisTemplate.delete(SystemConstants.redis_goods);
            int count = 0;
            for(Goods good : goodsList){
                redisTemplate.boundHashOps(SystemConstants.redis_goods).put(good.getId(),good);
                count ++;
            }
            sync = false;
            log.info("数据同步到Redis（redis_goods），共同步了：" + count + "条数据！<<<<<<<<<");
        }
        log.info(".........检查db_goods库（tb_sku表）结束.........");
        return sync;
    }

}
