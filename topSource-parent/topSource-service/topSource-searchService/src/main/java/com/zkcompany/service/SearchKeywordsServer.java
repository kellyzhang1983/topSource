package com.zkcompany.service;

import com.zkcompany.pojo.Goods;

import java.util.List;
import java.util.Map;

public interface SearchKeywordsServer {

    Map<String,Object> searchKeywords(Map<String,Object> keywordMap) throws Exception;

    List<String> analyzeSuggest(String suggest) throws Exception;
}
