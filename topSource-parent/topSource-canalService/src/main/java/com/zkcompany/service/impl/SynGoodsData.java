package com.zkcompany.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.Goods;
import com.zkcompany.service.ProcessGoodsData;
import com.zkcompany.uitl.ConvertObject;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
@Slf4j
public class SynGoodsData implements ProcessGoodsData {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private AnalyzeKeywordPinyin analyzeKeywordPinyin;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MinioClient minioClient;

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
            redisTemplate.boundHashOps(SystemConstants.redis_goods).put(goods.getId(),goods);
        } catch (Exception e) {
            log.error("Redis operation goods_addOrUpdateEs failed: " + e.getMessage());
        }
        try {
            //对品牌名称进行拼音分词
            AnalyzeRequest analyzeRequest = analyzeKeywordPinyin.searchAnalyzeQuery(goods.getBrandName());
            AnalyzeResponse analyze = elasticsearchClient.indices().analyze(analyzeRequest);
            //得到分词的集合，放入suggestion中
            goods.setSuggestion(analyzeKeywordPinyin.searchAnalyzeReuslt(analyze));

            IndexRequest<Goods> request = new IndexRequest.Builder<Goods>()
                    .index("tb_goods")
                    .id(goods.getId())
                    .document(goods)
                    .build();
            elasticsearchClient.index(request);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("ES operation goods_addOrUpdateEs  failed: " + e.getMessage() );
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

        Goods goods = (Goods)redisTemplate.boundHashOps(SystemConstants.redis_goods).get(id);
        //http://192.168.3.5:9000/topsource-goodslist/good_image_1745522458983.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=admin%2F20250424%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20250424T192141Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=b07a16a83b25b13aa7c72abf54304e38511a3f368dfae091809ba27aaf23388d
        if(!StringUtils.isEmpty(goods.getImage())){
            String goodsImage = goods.getImage();
            int beginIndexOf = goodsImage.lastIndexOf("/") + 1;
            int lastIndexOf = goodsImage.lastIndexOf("?");
            String image = goodsImage.substring(beginIndexOf, lastIndexOf);

            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket("topsource-goodslist")
                        .object(image)
                        .build());
            } catch (Exception e) {
                log.error("Minio operation goods_deleteMinio failed: " + e.getMessage());
            }
        }
        try {
            redisTemplate.boundHashOps(SystemConstants.redis_goods).delete(id);
        } catch (Exception e) {
            log.error("Redis operation goods_deleteEs failed: " + e.getMessage());
        }
        try {
            DeleteRequest request = new DeleteRequest.Builder()
                    .index("tb_goods")
                    .id(id)
                    .build();
            elasticsearchClient.delete(request);
        } catch (Exception e) {
            // 处理异常，例如记录日志或采取其他措施
            log.error("ES operation goods_deleteEs  failed: " + e.getMessage());
        }
    }
}
