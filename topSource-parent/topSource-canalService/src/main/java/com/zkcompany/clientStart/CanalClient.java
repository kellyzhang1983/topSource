package com.zkcompany.clientStart;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.listener.impl.ListenerMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@Slf4j
public class CanalClient {

    private CanalConnector connector;

    @Autowired
    private ListenerMessage listenerMessage;

    @Value("${canal.client.instances.host}")
    private String canalHost;

    @Value("${canal.client.instances.port}")
    private int canalPort;
    //监听的数据实例，由服务端配置，默认是example
    @Value("${canal.client.instances.destination}")
    private String destination;

    @Value("${canal.client.instances.username}")
    private String username;

    @Value("${canal.client.instances.password}")
    private String password;

    @Value("${canal.client.instances.batchSize}")
    private int batchSize;




    @PostConstruct
    public void start() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            // 创建 canal 连接器,链接服务端（必要参数：主机IP,端口《在服务端配置》）
            connector = CanalConnectors.newSingleConnector(
                    new java.net.InetSocketAddress(canalHost, canalPort),
                    destination,
                    username,
                    password
            );


            try {
                // 连接 canal 服务器
                connector.connect();
                // 订阅数据库和表，这里可以根据需求修改过滤规则
                connector.subscribe(".*\\..*");
                connector.rollback();


                while (true) {
                    // 获取消息
                    Message message = connector.getWithoutAck(batchSize);
                    //Message message = connector.get(100);
                    //获取批次ID，如果是-1那么没有消息
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        try {
                            log.info("当前线程休眠3秒钟：" +Thread.currentThread().getName());
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage());
                        }
                    } else {
                        // 处理消息
                        processMessage(message.getEntries());
                        //listenerMessage.addRedis(entries);
                        // 确认消息接收
                        connector.ack(batchId);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage() + "【canalServiceError:CanalClient】CanalClient.start()初始化连接canal失败！");
                e.printStackTrace();
            }
        });

    }


    private void processMessage(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                try {
                    CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                    CanalEntry.EventType eventType = rowChange.getEventType();
                    long num = rowChange.getRowDatasList().size();
                    String tableName = entry.getHeader().getTableName();
                    System.out.println(String.format("Binlog 文件名: %s, 位置: %s,操作表名:%s, 操作类型: %s, 执行条数：%s",
                            entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),tableName,eventType, num));
                    //同步后续数据进行处理
                    listenerMessage.processData(tableName,rowChange,eventType);
                } catch (Exception e) {
                    log.error(e.getMessage() + "【canalServiceError:CanalClient】CanalClient.processMessage()解析ROW级数据失败！");
                }
            }
        }
    }
}
