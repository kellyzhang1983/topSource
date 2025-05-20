package com.zkcompany.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Map;

public interface ProcessMarketActivity {

    void marketActivity_addOrUpdateRedis(List<CanalEntry.Column> columns);

    void marketActivity_deleteRedis(List<CanalEntry.Column> columns);
}
