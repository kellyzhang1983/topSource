package com.zkcompany.task;

import com.zkcompany.service.ProcessGoodsAllDataToEs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckMysqlToEsData {

    @Autowired
    private ProcessGoodsAllDataToEs processGoodsAllDataToEs;

    @Scheduled(cron = "0 18 21 * * ?")
    public void sysn() throws Exception{
        log.info("=====================开始同步=======================");
        Boolean syn = processGoodsAllDataToEs.goods_allDataSynToEs();
        if(!syn){
            log.info("Mysql数据库与索引库ES数据一致，不需要同步");
        }else{
            log.info("Mysql数据库与索引库ES数据不一致，需要同步");
        }
        log.info("=====================结束=======================");
    }
}
