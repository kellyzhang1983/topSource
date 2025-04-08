package com.zkcompany.service.impl;

import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnalyzeKeywordPinyin {

    public AnalyzeRequest searchAnalyzeQuery(String keyword){
        AnalyzeRequest analyzeRequest = new AnalyzeRequest.Builder()
                .index("tb_goods")
                .text(keyword)
                .analyzer("completion_analyzer")
                .build();
        return analyzeRequest;
    }

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
