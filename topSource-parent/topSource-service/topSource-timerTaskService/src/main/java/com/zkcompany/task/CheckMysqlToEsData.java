package com.zkcompany.task;

import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.service.GoodsDataBaseAndEsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckMysqlToEsData {

    @Autowired
    private GoodsDataBaseAndEsService goodsDataBaseAndEsService;

    @Scheduled(cron = "0 10 09 * * ?")
    public void checkTbGoodsTimerTask() {
        Boolean syn = null;
        try {
            syn = goodsDataBaseAndEsService.goodsDataSynToEs();
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【CheckMysqlToEsData.checkTbGoodsTimerTask】调用goodsDataSynToEs方法报错！");
        }
        if(!syn){
            log.info("=========结论: Mysql(tb_goods)数据库与索引库ES数据一致，不需要同步=========");
        }else{
            log.info("=========结论: Mysql(tb_goods)数据库与索引库ES数据不一致，需要同步=========");
        }
    }
}
