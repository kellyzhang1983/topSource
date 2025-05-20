package com.zkcompany.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

public interface ProcessShopCartData {

    void shopCart_addAndUpdateRedis(List<CanalEntry.Column> columns);

    void shopCart_deleteRedis(List<CanalEntry.Column> columns);
}
