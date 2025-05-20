package com.zkcompany.fallback;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.fegin.GoodsCenterFegin;
import com.zkcompany.pojo.Goods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoodsServerFallBack implements GoodsCenterFegin {

    @Override
    public Result upateGoodsNum(Goods goods) {
        log.error("GoodsCenterFegin（goods-server）：远程服务调用upateGoodsNum失败,请查看详细信息！.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"GoodsCenterFegin（goods-server）：远程服务调用upateGoodsNum失败,请查看详细信息！.....");
    }

    @Override
    public Result upateGoodsNumTimerTask(Goods goods) {
        log.error("GoodsCenterFegin（goods-server）：远程服务调用upateGoodsNumTimerTask失败,请查看详细信息！.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"GoodsCenterFegin（goods-server）：远程服务调用upateGoodsNumTimerTask失败,请查看详细信息！.....");
    }

    @Override
    public Result updateGoodsNumInventoryTimeTask(Goods goods, String flag) {
        log.error("GoodsCenterFegin（goods-server）：远程服务调用updateGoodsNumInventoryTimeTask失败,请查看详细信息！.....");
        return new Result<>(false, StatusCode.SC_INTERNAL_SERVER_ERROR,"GoodsCenterFegin（goods-server）：远程服务调用updateGoodsNumInventoryTimeTask失败,请查看详细信息！.....");
    }
}
