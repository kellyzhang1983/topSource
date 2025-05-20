package com.zkcompany.fallback;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.fegin.MarketCenterFegin;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MarketServerFallback implements MarketCenterFegin {


    @Override
    public Result reduceActivityGoods(ActivityGoods activityGoods) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"MarketCenterFegin（market-server）：远程服务(reduceActivityGoods)调用失败,请查看详细信息！.....");
    }

    @Override
    public Result addActivityGoods(ActivityGoods activityGoods) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"MarketCenterFegin（market-server）：远程服务(addActivityGoods)调用失败,请查看详细信息！.....");
    }

    @Override
    public Result addActivityStatus(Order order) {
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"MarketCenterFegin（market-server）：远程服务(addActivityStatus)调用失败,请查看详细信息！.....");
    }
}
