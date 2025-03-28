package com.zkcompany.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;


import java.util.List;

public interface ListenerMessage {
    void processData(String tableName,CanalEntry.RowChange rowChange,CanalEntry.EventType eventType);
}
