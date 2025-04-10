package com.zkcompany.controller;

import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.service.SearchKeywordsServer;
import org.apache.commons.lang3.ObjectUtils;
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
        Object page = keywords.get("page");
        Object pageSize = keywords.get("pageSize");
        if(!ObjectUtils.isEmpty(page)){
            try {
                Integer.valueOf(page.toString());
            } catch (NumberFormatException e) {
                throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"page转换成整数失败，请检查参数！");
            }
        }

        if(!ObjectUtils.isEmpty(pageSize)){
            try {
                Integer.valueOf(pageSize.toString());
            } catch (NumberFormatException e) {
                throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"pageSize转换成整数失败，请检查参数！");
            }
        }
        Map map = searchKeywordsServer.searchKeywords(keywords);
        return new Result(true, StatusCode.SC_OK,"查询成功",map);
    }

    @PostMapping("/suggest")
    public Result analyzeSuggest(@RequestBody Map keywords) throws Exception{
        List<String> suggestList = searchKeywordsServer.analyzeSuggest(keywords.get("keyword").toString());
        return new Result(true, StatusCode.SC_OK,"查询成功",suggestList);
    }
}
