package com.zkcompany.listener.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.service.ProcessGoodsData;
import com.zkcompany.service.ProcessOrderData;
import com.zkcompany.service.ProcessUserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ListenerMessage implements com.zkcompany.listener.ListenerMessage {

    @Autowired
    private ProcessUserData processUserData;

    @Autowired
    private ProcessOrderData processOrderData;

    @Autowired
    private ProcessGoodsData processGoodsData;

    private List<List<CanalEntry.Column>> columnList(CanalEntry.RowChange rowChange,CanalEntry.EventType eventType){
        List<List<CanalEntry.Column>> columnsTatail = new ArrayList<>();
        //判断监听类型，insert、delete、update
        if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE){
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()){
                //得到新增后或者修改后的数据，如果添加、修改的数据是多条。那么把一列的数据封装成集合，放在list的里面。
                List<CanalEntry.Column> columnsList = rowData.getAfterColumnsList();
                columnsTatail.add(columnsList);
            }
        }else if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()){
                //得到删除前的数据，如果删除的数据是多条。那么把一列的数据封装成集合，放在list的里面。
                List<CanalEntry.Column> columnsList = rowData.getBeforeColumnsList();
                columnsTatail.add(columnsList);
            }
        }

        return columnsTatail;
    }



    @Override
    public void processData(String tableName,CanalEntry.RowChange rowChange,CanalEntry.EventType eventType){

        List<List<CanalEntry.Column>> columnList = columnList(rowChange, eventType);
        //记录一对多的方法调用次数。
        int count = 0;
        //记录是哪个方法进行的调用
        String processMethod = "";
        //记录用户是否统一修改，修改后不需要再次修改(一对多的数据)；
        Map<String,String> userMap = new HashMap<>();
        for(List<CanalEntry.Column> List : columnList){
            //判断监听的表名字
            switch (tableName){
                //数据表是db_user
                case "tb_user":
                    //数据处理,加入Redis缓存中
                    if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE){
                        processUserData.user_addOrUpdateRedis(List);
                    }else if (eventType == CanalEntry.EventType.DELETE) {
                        processUserData.user_deleteRedis(List);
                    }
                    break;
                case "tb_user_points":
                    if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                        if(count == 0){
                            processMethod = "processUserData.point_addOrUpdateRedis方法";
                        }
                        count = processUserData.point_addOrUpdateRedis(List,userMap,count);
                    }else if (eventType == CanalEntry.EventType.DELETE) {
                        processUserData.point_deleteRedis(List,userMap,count);
                    }
                    break;
                case "tb_order":
                    if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                        if(count == 0){
                            processMethod = "processOrderData.order_addOrUpdateRedis方法";
                        }
                        count = processOrderData.order_addOrUpdateRedis(List,userMap,count);
                    }else if (eventType == CanalEntry.EventType.DELETE){
                        processOrderData.order_deleteRedis(List);
                    }
                    break;
                case "tb_user_roles":
                    if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                        if(count == 0){
                            processMethod = "processUserData.user_addRole方法";
                        }
                        count =  processUserData.user_addRole(List,userMap,count);
                    }else if (eventType == CanalEntry.EventType.DELETE){
                        processUserData.user_deleteRole(List);
                    }
                    break;
                case "tb_sku":
                    if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE){
                        processGoodsData.goods_addOrUpdateEs(List);
                    }else if (eventType == CanalEntry.EventType.DELETE) {
                        processGoodsData.goods_deleteEs(List);
                    }
            }
        }
        if(count != 0) {
            log.info(processMethod + "一对多的数据总共执行：" + count);
        }

    }
}
