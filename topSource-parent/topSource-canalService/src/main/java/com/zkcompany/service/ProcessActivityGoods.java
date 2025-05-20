package com.zkcompany.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Map;

public interface ProcessActivityGoods {

    int activityGoods_addOrUpdateRedis(List<CanalEntry.Column> columns, Map<String,String> activityGoodsMap, Integer count);

    void activityGoods_deleteRedis(List<CanalEntry.Column> columns);
}
