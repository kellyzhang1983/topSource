package com.zkcompany.service.impl;

import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnalyzeKeywordPinyin {
    /**
     * GET /tb_goods/_analyze
     * {
     *   "text": "蓝光眼镜",
     *   "analyzer": "completion_analyzer"
     * }
     * 利用_analyze进行分词、按照上述DSL语句组装ES Query；
     * ***/
    public AnalyzeRequest searchAnalyzeQuery(String keyword){
        AnalyzeRequest analyzeRequest = new AnalyzeRequest.Builder()
                .index("tb_goods")
                .text(keyword)
                .analyzer("completion_analyzer")
                .build();
        return analyzeRequest;
    }

    /***
     * {
     *   "tokens": [
     *     {
     *       "token": "h",
     *       "start_offset": 0,
     *       "end_offset": 2,
     *       "type": "word",
     *       "position": 0
     *     },
     *     {
     *       "token": "w",
     *       "start_offset": 0,
     *       "end_offset": 2,
     *       "type": "word",
     *       "position": 1
     *     },
     *     {
     *       "token": "华为",
     *       "start_offset": 0,
     *       "end_offset": 2,
     *       "type": "word",
     *       "position": 1
     *     },
     *     {
     *       "token": "huawei",
     *       "start_offset": 0,
     *       "end_offset": 2,
     *       "type": "word",
     *       "position": 1
     *     },
     *     {
     *       "token": "hw",
     *       "start_offset": 0,
     *       "end_offset": 2,
     *       "type": "word",
     *       "position": 1
     *     }
     *   ]
     * }
     * 对查询出的结果进行解析，只需要解析token，获得解析出来的全拼、拼音首字母以及名字
     * ***/
    public List<String> searchAnalyzeReuslt(AnalyzeResponse analyze){
        List<AnalyzeToken> tokens = analyze.tokens();
        List<String> suggestion = new ArrayList<>();
        for(AnalyzeToken token : tokens){
            String analyzeToken = token.token();
            if(analyzeToken.length() > 1){
                suggestion.add(analyzeToken);
            }
        }
        return suggestion;
    }
}
