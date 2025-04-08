package com.zkcompany.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.pojo.Goods;
import com.zkcompany.service.ProcessGoodsData;
import com.zkcompany.uitl.ConvertObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j

public class SynGoodsData implements ProcessGoodsData {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private AnalyzeKeywordPinyin analyzeKeywordPinyin;

    @Override
    public void goods_addOrUpdateEs(List<CanalEntry.Column> columns) {
        Goods goods = new Goods();
        for (CanalEntry.Column column : columns){
            //得到列的名称
            String name = column.getName();
            //得到列的值
            String value = column.getValue();
            //转换成USER对象
            ConvertObject.convertGoods(goods,name,value);
        }
        try {
            AnalyzeRequest analyzeRequest = analyzeKeywordPinyin.searchAnalyzeQuery(goods.getBrandName());
            AnalyzeResponse analyze = elasticsearchClient.indices().analyze(analyzeRequest);
            goods.setSuggestion(analyzeKeywordPinyin.searchAnalyzeReuslt(analyze));

            IndexRequest<Goods> request = new IndexRequest.Builder<Goods>()
                    .index("tb_goods")
                    .id(goods.getId())
                    .document(goods)
                    .build();
            elasticsearchClient.index(request);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            e.printStackTrace();
            System.err.println("ES operation addOrUpdateES  failed: " + e.getMessage() );
        }
    }

    @Override
    public void goods_deleteEs(List<CanalEntry.Column> columns) {
        String id = "";

        for (CanalEntry.Column column : columns){
            switch (column.getName()){
                case "id":
                    id = column.getValue();
                    break;
            }
        }
        try {
            DeleteRequest request = new DeleteRequest.Builder()
                    .index("tb_goods")
                    .id(id)
                    .build();
            elasticsearchClient.delete(request);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            System.err.println("ES operation addOrUpdateES  failed: " + e.getMessage());
        }
    }
}
