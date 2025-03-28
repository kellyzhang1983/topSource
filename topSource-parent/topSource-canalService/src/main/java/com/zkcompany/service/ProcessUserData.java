package com.zkcompany.service;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Map;

public interface ProcessUserData {

    void user_addOrUpdateRedis(List<CanalEntry.Column> columns);

    void user_deleteRedis(List<CanalEntry.Column> columns);

    Integer point_addOrUpdateRedis(List<CanalEntry.Column> columns, Map<String,String> userMap, Integer count);

    Integer point_deleteRedis(List<CanalEntry.Column> columns, Map<String,String> userMap, Integer count);

    Integer user_addRole(List<CanalEntry.Column> columns , Map<String,String> userMap,Integer count);

    void user_deleteRole(List<CanalEntry.Column> columns);
}
