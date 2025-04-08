package com.zkcompany.controller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.service.SearchKeywordsServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/search")
@RestController
public class SearchKeyWordsController {

    @Autowired
    private SearchKeywordsServer searchKeywordsServer;

    @PostMapping("/keywords")
    public Result searchKeywords(@RequestBody Map keywords) throws Exception{
        Map map = searchKeywordsServer.searchKeywords(keywords);
        return new Result(true, StatusCode.SC_OK,"查询成功",map);
    }

    @PostMapping("/suggest")
    public Result analyzeSuggest(@RequestBody Map keywords) throws Exception{
        List<String> suggestList = searchKeywordsServer.analyzeSuggest(keywords.get("keyword").toString());
        return new Result(true, StatusCode.SC_OK,"查询成功",suggestList);
    }
}
