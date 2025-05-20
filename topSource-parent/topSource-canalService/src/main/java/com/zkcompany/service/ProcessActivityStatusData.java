package com.zkcompany.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

public interface ProcessActivityStatusData {

    void activityStatus_addOrUpdateRedis(List<CanalEntry.Column> columns);

    void activityStatus_deleteRedis(List<CanalEntry.Column> columns);
}
