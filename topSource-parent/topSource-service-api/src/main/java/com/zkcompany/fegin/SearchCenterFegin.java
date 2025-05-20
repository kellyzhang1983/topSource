package com.zkcompany.fegin;

import com.zkcompany.config.FeignApiConfig;
import com.zkcompany.entity.Result;
import com.zkcompany.fallback.SearchSeverFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "search-server",configuration = FeignApiConfig.class, fallback = SearchSeverFallBack.class)
public interface SearchCenterFegin {

    @PostMapping("/search/keywords")
    public Result searchKeywords(@RequestBody Map keywords);

    @PostMapping("/suggest")
    public Result analyzeSuggest(@RequestBody Map keywords);
}
