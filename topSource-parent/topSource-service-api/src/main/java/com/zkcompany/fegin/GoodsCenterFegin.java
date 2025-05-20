package com.zkcompany.fegin;

import com.zkcompany.annotation.InnerMethodCall;
import com.zkcompany.config.FeignApiConfig;
import com.zkcompany.entity.Result;
import com.zkcompany.fallback.GoodsServerFallBack;
import com.zkcompany.pojo.Goods;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "goods-server",configuration = FeignApiConfig.class, fallback = GoodsServerFallBack.class)
public interface GoodsCenterFegin {

    @PutMapping("/goods/updateGoodsNum")
    public Result upateGoodsNum(@RequestBody Goods goods);

    @PutMapping("/goods/upateGoodsNumTimerTask")
    @InnerMethodCall
    public Result upateGoodsNumTimerTask(@RequestBody Goods goods);

    @PutMapping("/goods/updateGoodsNumInventoryTimeTask")
    @InnerMethodCall
    public Result updateGoodsNumInventoryTimeTask(@RequestBody Goods goods,
                                                  @RequestParam String flag);
}
