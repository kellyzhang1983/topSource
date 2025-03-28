package com.zkcompany.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Map;

public interface ProcessOrderData {

    Integer order_addOrUpdateRedis(List<CanalEntry.Column> columns, Map<String,String> userMap,Integer count);

    void order_deleteRedis(List<CanalEntry.Column> columns);
}
