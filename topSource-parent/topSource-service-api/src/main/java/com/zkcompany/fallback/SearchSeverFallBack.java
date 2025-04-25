package com.zkcompany.fallback;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.fegin.SearchCenterFegin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SearchSeverFallBack implements SearchCenterFegin {

    @Override
    public Result searchKeywords(Map keywords) {
        log.error("SearchCenterFegin（search-server）：远程服务调用searchKeywords失败,请查看详细信息！.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"SearchCenterFegin（search-server）：远程服务调用失败,请查看详细信息！.....");
    }

    @Override
    public Result analyzeSuggest(Map keywords) {
        log.error("SearchCenterFegin（search-server）：远程服务调用analyzeSuggest失败,请查看详细信息！.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"SearchCenterFegin（search-server）：远程服务调用失败,请查看详细信息！.....");
    }
}
