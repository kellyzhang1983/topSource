package com.zkcompany.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

public interface ProcessGoodsData {
    void goods_addOrUpdateEs(List<CanalEntry.Column> columns);

    void goods_deleteEs(List<CanalEntry.Column> columns);
}
