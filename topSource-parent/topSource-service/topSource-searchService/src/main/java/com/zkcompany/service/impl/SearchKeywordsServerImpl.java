package com.zkcompany.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.entity.PageHelp;
import com.zkcompany.pojo.Goods;
import com.zkcompany.service.SearchKeywordsServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
/**
 * component: ES搜索查询（分词、过滤、查询条件）
 * auther:zk
 * 1、针对关键字进行查询，关键字需要算分，如果关键字为空，默认查询全部
 * 2、过滤条件有品牌，价格（范围）[不分词]，如果为空则不过滤，过滤条件不参与算分
 * 3、对商品isWeightAdd为True的字段增加权重信息，搜索靠前，增加权重规则:avg【击中+log(销量*1.2)】
 * 4、对name、brand_name、spec【size、version、taste、color、spec_search】进行高亮显示
 * 5、
 * **/
@Service
@Slf4j
public class SearchKeywordsServerImpl implements SearchKeywordsServer, UserDetailsService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public Map<String,Object> searchKeywords(Map<String,Object> keywordMap) throws Exception {
        // 1. 构建查询条件
        Query searchQuery = createSearchQuery(keywordMap);

        //2. 构建聚合函数，搜索到的文档对品牌和价格范围进行统计
        Aggregation aggregationSearchQuery = createAggregationSearchQuery();

        // 3. 构建返回的元素高亮
        Highlight highlight = new Highlight.Builder()
                .fields("brand_name", new HighlightField.Builder().requireFieldMatch(false).build())
                .fields("name", new HighlightField.Builder().requireFieldMatch(false).build())
                .fields("color", new HighlightField.Builder().requireFieldMatch(false).build())
                .fields("size", new HighlightField.Builder().requireFieldMatch(false).build())
                .fields("version", new HighlightField.Builder().requireFieldMatch(false).build())
                .fields("taste", new HighlightField.Builder().requireFieldMatch(false).build())
                .fields("spec_search", new HighlightField.Builder().requireFieldMatch(false).build())
                .build();

        // 4. 检查分页信息以及非空处理
        Integer page = keywordMap.get("page") == null ? 0 : Integer.valueOf(keywordMap.get("page").toString());
        Integer pageSize = keywordMap.get("pageSize") == null ? 50 : Integer.valueOf(keywordMap.get("pageSize").toString());
        Map<String, Integer> pageHelpMap = PageHelp.decidePage(page, pageSize);

        // 5. 构建完整搜索请求
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("tb_goods")
                .query(searchQuery)
                .from((pageHelpMap.get("page") - 1) * pageHelpMap.get("pageSize"))
                .size(pageHelpMap.get("pageSize"))
                .highlight(highlight)
                .aggregations("brandAgg",aggregationSearchQuery)
                .build();

        // 6. 发送请求，得到ES数据
        SearchResponse<Goods> search = elasticsearchClient.search(searchRequest, Goods.class);

        //7。封装Map,返回给前端
        long totalNum = search.hits().total().value();
        List<Goods> goods = convertResponseToGoodsList(search);
        Map<String, Object> aggregatetion = convertResponseAggregationToMap(search);


        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("total",totalNum);
        resultMap.put("page",pageHelpMap.get("page"));
        resultMap.put("pageSize",pageHelpMap.get("pageSize"));
        resultMap.put("dataList",goods);
        resultMap.put("brandList",aggregatetion.keySet());
        resultMap.put("aggs",aggregatetion);

        return resultMap;
    }
    /***
     * 用拼音补全名称：注意（拼音补全针对的是品牌，例如：“hw”,所有拼音首字母缩写为hw的商品都可以搜索出来）
     * ES查询语句：
     * GET /tb_goods/_search
     * {
     *     "suggest": {
     *       "goods_suggest": {
     *         "text": "mtj",
     *         "completion": {
     *           "field": "suggestion",
     *           "size":10
     *         }
     *       }
     *     }
     * }
     * **/
    @Override
    public List<String> analyzeSuggest(String suggest) throws Exception {

        //1、创建搜索条件，根据上述的查询语句构建SearchRequest对象，
        FieldSuggester fieldSuggester = new FieldSuggester.Builder()
                .completion(new CompletionSuggester.Builder()
                        .field("suggestion")
                        .size(10)
                        .build())
                .build();

        Suggester suggester = new Suggester.Builder()
                .suggesters("goods_suggest",fieldSuggester)
                .text(suggest)
                .build();

        SearchRequest searchRequest = new SearchRequest.Builder()
                .suggest(suggester)
                .build();
        //2、发送请求，得到ES数据
        SearchResponse<Goods> searchResponse = elasticsearchClient.search(searchRequest, Goods.class);
        Map<String, List<Suggestion<Goods>>> suggestMap = searchResponse.suggest();
        List<Suggestion<Goods>> suggestionList = suggestMap.get("goods_suggest");
        List<String> nameList = new ArrayList<>();

        if (suggestionList == null) {
            return nameList;
        }

        /***
         * 按照上述结构，解析查询出来的结果
         *"suggest": {
         *     "goods_suggest": [
         *       {
         *         "text": "mtj",
         *         "offset": 0,
         *         "length": 3,
         *         "options": [
         *           {
         *             "text": "mtj",
         *             "_index": "tb_goods",
         *             "_id": "10095334710",
         *             "_score": 1,
         *             "_source": {
         *               "id": "10095334710",
         *               "name": "梦特娇/Montagut单肩女包 新款菱格纹链条带子女包 女士时尚潮流斜挎包 女士小包包 绿色",
         *               "price": 67,
         *               "num": 10000,
         *               "alert_num": 100,
         *               "image": "https://m.360buyimg.com/mobilecms/s720x720_jfs/t6535/77/677392411/241066/8a1f5368/594355bcNaad2545d.jpg!q70.jpg.webp",
         *               "weight": 10,
         *               "create_time": "2019-05-01 08:00:00",
         *               "update_time": "2019-05-01 08:00:00",
         *               "brand_name": "梦特娇",
         *               "sale_num": 0,
         *               "comment_num": 0,
         *               "status": "1",
         *               "isAddWeight": "false",
         *               "color": "绿色",
         *               "suggestion": [
         *                 "梦特娇",
         *                 "mengtejiao",
         *                 "mtj"
         *               ]
         *             }
         *           }
         *         }
         *       ]
         *   }
         * ***/
        for(Suggestion<Goods> suggestion : suggestionList){
            List<CompletionSuggestOption<Goods>> options = suggestion.completion().options();
            for(CompletionSuggestOption<Goods> option : options){
                Goods goods = option.source();
                if(!ObjectUtils.isEmpty(goods)){
                    nameList.add(goods.getName());
                }
            }
        }
        return nameList;
    }


    private Aggregation createAggregationSearchQuery(){
        Aggregation aggregation = new Aggregation.Builder()
                .terms(new TermsAggregation.Builder()
                        .field("brand_name")
                        .build())
                .aggregations("salesTotal_num",new Aggregation.Builder()
                        .sum(new SumAggregation.Builder()
                                .field("sale_num")
                                .build())
                        .build())
                .aggregations("price_stats",new Aggregation.Builder()
                        .stats(new StatsAggregation.Builder()
                                .field("price")
                                .build())
                        .build())
                .build();
        return aggregation;
    }


    private Query createSearchQuery(Map<String,Object> keywordMap){
        /**
         * 详细说明查询条件的构建：
         * 1. 先看DSL语句：
         * GET /tb_goods/_search
         * {
         *   "query": {
         *     "function_score": {
         *       "query": {
         *         "bool":{
         *           "must": [{"match": {"all": keyword}}]
         *           "filter": [{"range": {"price": {"gte": 230,"lte": 3200}}}]
         *         }
         *       },
         *       "functions": [{
         *         "filter":{"term":{"id":"10010868291"}},
         *         "field_value_factor": {
         *           "field":"sale_num",
         *           "modifier": "log1p",
         *           "factor": 1.2,
         *           "missing": 0
         *         }
         *       }],
         *       "boost_mode": "avg"
         *     }
         *   },
         *   "from": 0,
         *   "size": 20,
         *   "highlight": {
         *     "fields": {"brand_name": {"require_field_match": "false"},
         *       "name": {"require_field_match": "false"},
         *       "color": {"require_field_match": "false"},
         *       "size": {"require_field_match": "false"},
         *       "version": {"require_field_match": "false"}
         *     }
         *   }
         * }
         * 2。用对象构建JOSN数据结构成对象，
         * （1）主查询语句：BoolQuery boolQuery对象，{"match": {"all": keyword}}先对keyword进行分词（分词使用IK分词器），然后在进行搜索
         * （2）functions，改变查询出来内容的权重，functions(new FunctionScore.Builder() ......1、设置对哪个字段改变权重，2.改变权重的方式，3。field_value_factor代表是通过文档的字段改变，用的是sale_num销量这个字段改变权重
         * （3）高亮显示highlight，通过加标签<em></em>来显示高亮，fields需要显示的字段； Highlight highlight = new Highlight.Builder()
         * （4）from、size是分页，相当于MYSQL的limit
         * （5）关于用API构建ES的查询条件，比较复杂，主要思想就是跟着上面的DSL语句进行一层一层的嵌套对象，进行链式编程！想学习的朋友可以先了解基础
         * **/
        //1. 解析map里面的参数
        Object keyword = keywordMap.get("keyword");
        Object minPrice = keywordMap.get("minPrice");
        Object maxPrice = keywordMap.get("maxPrice");
        Object brandName = keywordMap.get("brandName");
        //2. 构建ES中bool查询的must，filter中的Query对象
        /**
         *"must": [{"match": {"all": "艾森诺老花镜"}}],
         *"filter": [{"range": {"price": {"gte": 180,"lte": 200}}}
         *           {"match": {"brand_name":"艾森诺"}}
         * ]
         * **/
        Map<String, List<Query>> searhQueryMap = searchAllParms(keyword, minPrice, maxPrice, brandName);

        BoolQuery boolQuery = null;
        //如果都为空，那么不需要filter(searhQueryMap.get("filter"))
        if((ObjectUtils.isEmpty(minPrice) || ObjectUtils.isEmpty(maxPrice)) && ObjectUtils.isEmpty(brandName)) {
            boolQuery = new BoolQuery.Builder()
                    .must(searhQueryMap.get("must"))
                    .build();
        }else{
            boolQuery = new BoolQuery.Builder()
                    .must(searhQueryMap.get("must"))
                    .filter(searhQueryMap.get("filter"))
                    .build();
        }

        Query functionQuery = new Query.Builder()
                .bool(boolQuery)
                .build();

        //对isAddWeight = true 的产品进行加权重，加权重的方式:利用销量进行加权重，销量越高，权重越大
        FunctionScoreQuery functionScore = new FunctionScoreQuery.Builder()
                .query(functionQuery)
                .functions(new FunctionScore.Builder()
                        .filter(new Query.Builder()
                                .term(new TermQuery.Builder()
                                        .field("isAddWeight")
                                        .value("true")
                                        .build())
                                .build())
                        .fieldValueFactor(new FieldValueFactorScoreFunction.Builder()
                                .field("sale_num")
                                .modifier(FieldValueFactorModifier.Log1p)
                                .factor(1.2)
                                .missing(0.0)
                                .build())
                        .build())
                .boostMode(FunctionBoostMode.Avg)
                .build();

        Query query = new Query.Builder()
                .functionScore(functionScore)
                .build();
        return query;
    }

    private Map<String,List<Query>> searchAllParms(Object keyword_must,
                                                   Object minPrice_filter,Object maxPrice_filter,Object brandName_filter){
        Query mustQuery = null;
        List<Query> mustQueryList = new ArrayList<>();
        //判断是否为空，如果为空MatchAllQuery.Builder()查询全部，不为空构建MatchQuery.Builder()查询all字段进行分词查询
        if (ObjectUtils.isEmpty(keyword_must)){
            mustQuery = new Query.Builder()
                    .matchAll(new MatchAllQuery.Builder()
                            .build())
                    .build();
        }else {
            mustQuery = new Query.Builder()
                    .match(new MatchQuery.Builder()
                            .field("all")
                            .query(keyword_must.toString())
                            .build())
                    .build();
            mustQueryList.add(mustQuery);
        }
        //判断是否为空，RangeQuery.Builder()创建价格过滤（范围过滤），brandName_filter查询brand_name字段进行分词查询
        Query filterQuery = null;
        List<Query> filterQueryList = new ArrayList<>();
        if(!(ObjectUtils.isEmpty(minPrice_filter) && ObjectUtils.isEmpty(maxPrice_filter))){
            filterQuery = new Query.Builder()
                    .range(new RangeQuery.Builder()
                            .number(new NumberRangeQuery.Builder()
                                    .field("price")
                                    .gte(Double.valueOf(minPrice_filter.toString()))
                                    .lte(Double.valueOf(maxPrice_filter.toString()))
                                    .build())
                            .build())
                    .build();
            filterQueryList.add(filterQuery);
        }

        if(!ObjectUtils.isEmpty(brandName_filter)){
            filterQuery = new Query.Builder()
                    .match(new MatchQuery.Builder()
                            .field("brand_name")
                            .query(brandName_filter.toString())
                            .build())
                    .build();
            filterQueryList.add(filterQuery);
        }
        //封装到MAP对象中，must和filter，对应查询条件的must和filter
        Map<String,List<Query>> searhQueryMap = new HashMap<String,List<Query>>();
        searhQueryMap.put("must",mustQueryList);
        searhQueryMap.put("filter",filterQueryList);
        return searhQueryMap;
    }

    private Map<String,Object> convertResponseAggregationToMap(SearchResponse<Goods> searchResponse){
        /**
         * "aggregations": {
         *     "brandAgg": {
         *       "doc_count_error_upper_bound": 0,
         *       "sum_other_doc_count": 61,
         *       "buckets": [
         *         {
         *           "key": "华为",
         *           "doc_count": 158,
         *           "total_num": {
         *             "value": 92410
         *           },
         *           "price_stats": {
         *             "count": 158,
         *             "min": 100,
         *             "max": 998,
         *             "avg": 595.3544303797469,
         *             "sum": 94066
         *           }
         *         },
         *    }
         * }
         * 1、解析聚合条件查询出来的结构，包括品牌、总销量、价格范围
         * **/
        Map<String,Object> aggregatetionValue = new HashMap<String,Object>();
        if(searchResponse.aggregations() != null && searchResponse.aggregations().get("brandAgg") != null){
            // 获取聚合结果
            List<StringTermsBucket> bucketValueAggregate = searchResponse.aggregations().get("brandAgg").sterms().buckets().array();
            //循环、遍历buckets统计出来的结果，
            for(StringTermsBucket termsBucket : bucketValueAggregate){
                JSONObject bucketValue = new JSONObject();
                //得到销量总数、"total_num": {"value": 92410}
                Double salesTotal_num = termsBucket.aggregations().get("salesTotal_num").sum().value();
                bucketValue.put("salesTotal_num",salesTotal_num.intValue());
                //得到价格范围 "price_stats": {"min": 100,"max": 998,"avg": 595.3544303797469}
                StatsAggregate price_stats = termsBucket.aggregations().get("price_stats").stats();
                String price_min = String.format("%.2f", price_stats.min());
                String price_max = String.format("%.2f", price_stats.max());
                bucketValue.put("price_min",price_min);
                bucketValue.put("price_max",price_max);
                //放入MAP中，数据结构{"华为":{"total_num":"92410"，"price_min":"100","price_max":"998"}}
                aggregatetionValue.put(termsBucket.key().stringValue(),bucketValue);
            }

        }
        return aggregatetionValue;
    }

    private List<Goods> convertResponseToGoodsList(SearchResponse<Goods> searchResponse) {
        // 1. 准备返回的List
        List<Goods> goodsList = new ArrayList<>();

        // 2. 检查是否有命中结果
        if (searchResponse.hits() == null || searchResponse.hits().hits() == null) {
            return goodsList; // 无结果时返回空List
        }

        // 3. 遍历所有命中的文档
        List<Hit<Goods>> hitList = searchResponse.hits().hits();
        for(Hit<Goods> hit : hitList ){
            // 3.1 获取原始Goods对象（可能为null）
            Goods goods = hit.source();
            if (goods == null) {
                continue;
            }

            // 3.2 处理高亮字段（如果有）
            if (hit.highlight() != null) {
                processHighlightFields(goods, hit.highlight());
            }

            // 3.3 添加到结果列表
            goodsList.add(goods);
        }
        return goodsList;
    }

    private void processHighlightFields(Goods goods, Map<String, List<String>> highlight) {
        // 1. 处理brand_name高亮
        if (highlight.containsKey("brand_name")) {
            List<String> brandHighlights = highlight.get("brand_name");
            if (!brandHighlights.isEmpty()) {
                goods.setBrandName(brandHighlights.get(0)); // 取第一个高亮片段
            }
        }

        // 2. 处理name高亮
        if (highlight.containsKey("name")) {
            List<String> nameHighlights = highlight.get("name");
            if (!nameHighlights.isEmpty()) {
                goods.setName(nameHighlights.get(0));
            }
        }

        // 其他字段（color/size/version等）同理...
        if (highlight.containsKey("color")) {
            List<String> colorHighlights = highlight.get("color");
            if (!colorHighlights.isEmpty()) {
                goods.setColor(colorHighlights.get(0));
            }
        }

        if (highlight.containsKey("size")) {
            List<String> sizeHighlights = highlight.get("size");
            if (!sizeHighlights.isEmpty()) {
                goods.setSize(sizeHighlights.get(0));
            }
        }

        if (highlight.containsKey("version")) {
            List<String> versionHighlights = highlight.get("version");
            if (!versionHighlights.isEmpty()) {
                goods.setVersion(versionHighlights.get(0));
            }
        }

        if (highlight.containsKey("spec_search")) {
            List<String> specSearchHighlights = highlight.get("spec_search");
            if (!specSearchHighlights.isEmpty()) {
                goods.setSpec_search(specSearchHighlights.get(0));
            }
        }

        if (highlight.containsKey("taste")) {
            List<String> tasteHighlights = highlight.get("taste");
            if (!tasteHighlights.isEmpty()) {
                goods.setTaste(tasteHighlights.get(0));
            }
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
