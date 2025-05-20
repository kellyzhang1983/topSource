package com.zkcompany.fegin;

import com.zkcompany.annotation.InnerMethodCall;
import com.zkcompany.config.FeignApiConfig;
import com.zkcompany.entity.Result;
import com.zkcompany.fallback.MarketServerFallback;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "market-server",configuration = FeignApiConfig.class, fallback = MarketServerFallback.class)
public interface MarketCenterFegin {
    @PutMapping("/market/reduceActivityGoods")
    @InnerMethodCall
    public Result reduceActivityGoods(@RequestBody ActivityGoods activityGoods);

    @PutMapping("/market/addActivityGoods")
    @InnerMethodCall
    public Result addActivityGoods(@RequestBody ActivityGoods activityGoods);

    @PostMapping("/market/addActivityStatus")
    @InnerMethodCall
    public Result addActivityStatus(@RequestBody Order order);
}
