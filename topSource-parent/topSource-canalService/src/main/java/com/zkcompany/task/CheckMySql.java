package com.zkcompany.task;

import com.zkcompany.service.CheckUserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckMySql {

    @Autowired
    private CheckUserData checkUserData;
    @Scheduled(cron = "0 17 0 * * ?")
    private void checkMySql(){
        log.info("=====================启动数据库、Redis的检查=======================");
        log.info("1、检查tb_user表与Redis是否一致==========================");
        boolean check_db_user = checkUserData.check_tb_user();
        if(check_db_user){
            log.info("tb_user与Redis一致，不需要同步......");
        }
        log.info("1、检查tb_user表与Redis结束！！！========================");

        log.info("2、检查tb_user_point表与Redis是否一致=====================");
        boolean check_tb_user_point = checkUserData.check_tb_user_point();
        if(check_tb_user_point){
            log.info("tb_user_point与Redis一致，不需要同步......");
        }
        log.info("2、检查check_tb_user_point表与Redis结束！！！");

        log.info("3、检查tb_user_role表与Redis是否一致");
        boolean check_tb_user_role = checkUserData.check_tb_user_role();
        if(check_tb_user_role){
            log.info("tb_user_role与Redis一致，不需要同步......");
        }
        log.info("3、检查tb_user_role表与Redis结束！！！=====================");

        log.info("4、检查tb_order表与Redis是否一致");
        boolean check_tb_order = checkUserData.check_tb_order();
        if(check_tb_order){
            log.info("tb_order与Redis一致，不需要同步......");
        }

        boolean check_tb_userOrder = checkUserData.check_tb_userOrder();
        if(check_tb_userOrder){
            log.info("tb_order与Redis一致，不需要同步......");
        }
        log.info("4、检查tb_order表与Redis结束！！！");

        log.info("=====================结束检查=======================");

    }
}
