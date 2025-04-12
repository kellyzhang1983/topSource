package com.zkcompany.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.dao.GoodsMapper;
import com.zkcompany.pojo.Goods;
import com.zkcompany.service.ProcessGoodsAllDataToEs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProcessGoodsAllDataToEsImpl implements ProcessGoodsAllDataToEs {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private AnalyzeKeywordPinyin analyzeKeywordPinyin;

    @Override
    public Boolean goods_allDataSynToEs() throws Exception{
        List<Goods> goodsList = goodsMapper.selectAllGoods();
        boolean syn = false;
        /**
         *GET /tb_goods/_search
         * {
         *   "query": {
         *     "match_all": {
         *     }
         *   }
         * }
         * 1. 构造这种结构，由于只查询数量。所以选择countRequest对象。
         * 2. 指定索引库名称。
         * 3. 构造query(new Query.Builder())
         * 4. 构造match_all（new MatchAllQuery.Builder().build()）
         * 5。 elasticsearchClient.count 查询数量
         * **/
        CountRequest countRequest = new CountRequest.Builder()
                .index("tb_goods")
                .query(new Query.Builder()
                        .matchAll(new MatchAllQuery.Builder()
                                .build())
                        .build())
                .build();

        CountResponse countResponse = elasticsearchClient.count(countRequest);
        long count = countResponse.count();
        if(count != goodsList.size()){
            log.info("开始同步到ES中........一共要同步：" + goodsList.size() + "条数据！");
            syn = true;
            List<BulkOperation> operations = new ArrayList<>();
            for(Goods good : goodsList){

                if(!StringUtils.isEmpty(good.getBrandName())) {
                    //good.setSuggestion(good.getBrandName());
                    //对品牌名称进行拼音分词
                    AnalyzeRequest analyzeRequest = analyzeKeywordPinyin.searchAnalyzeQuery(good.getBrandName());
                    AnalyzeResponse analyze = elasticsearchClient.indices().analyze(analyzeRequest);
                    //得到分词的集合，放入suggestion中
                    good.setSuggestion(analyzeKeywordPinyin.searchAnalyzeReuslt(analyze));
                }
                if(!StringUtils.isEmpty(good.getSpec())) {
                    JSONObject jsonObject = JSONObject.parseObject(good.getSpec());
                    Object taste = jsonObject.get("口味");
                    if (!ObjectUtils.isEmpty(taste)) {
                        good.setTaste(taste.toString());
                    }

                    Object color = jsonObject.get("颜色");
                    if (!ObjectUtils.isEmpty(color)) {
                        good.setColor(color.toString());
                    }

                    Object version = jsonObject.get("版本");
                    if (!ObjectUtils.isEmpty(version)) {
                        good.setVersion(version.toString());
                    }

                    Object size_1 = jsonObject.get("尺寸");
                    if (!ObjectUtils.isEmpty(size_1)) {
                        good.setSize(size_1.toString());
                    }

                    Object size_2 = jsonObject.get("尺码");
                    if (!ObjectUtils.isEmpty(size_2)) {
                        good.setSize(size_2.toString());
                    }

                    Object size_3 = jsonObject.get("内存");
                    if (!ObjectUtils.isEmpty(size_3)) {
                        good.setSize(size_3.toString());
                    }

                    Object spec = jsonObject.get("规格");
                    if (!ObjectUtils.isEmpty(spec)) {
                        good.setSpec_search(spec.toString());
                    }
                }
                good.setSpec(null);
                /**
                 * 批量插入：
                 * 1. 首先创建IndexOperation对象，确定索引库以及文档内容
                 * 2. 创建BulkOperation的对象，把单个对象放入BulkOperation中
                 * 3. 把BulkOperation放入集合中
                 * 4. 最终生成BulkRequest对象
                 * 5. elasticsearchClient.bulk(bulkRequest);执行批量插入操作
                 * **/
                IndexOperation<Goods> indexOp = new IndexOperation.Builder<Goods>()
                        .index("tb_goods")
                        .id(good.getId())
                        .document(good)
                        .build();

                BulkOperation bulkOp = (BulkOperation) new BulkOperation.Builder()
                        .index(indexOp)
                        .build();
                operations.add(bulkOp);

            }
            BulkRequest bulkRequest = new BulkRequest.Builder()
                    .operations(operations)
                    .build();
            elasticsearchClient.bulk(bulkRequest);
        }
        return syn;
    }
}
