package com.zkcompany.controller;
import com.zkcompany.annotation.Inner;
import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.Goods;
import com.zkcompany.service.GoodsService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@RequestMapping("/goods")
@CrossOrigin
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @PreAuthorize("hasAnyRole('user')")
    @PostMapping("/addGoods")
    public Result addGoods(
            @ModelAttribute Goods goods,
            @RequestParam("goodsImage") MultipartFile goodsImage){
        if(ObjectUtils.isEmpty(goodsImage)){
            new BusinessException(StatusCode.SC_NOT_FOUND,"goodsImage 参数为空！请传入goodsImage参数");
        }else{
            long goodsFileSize = goodsImage.getSize() / 1024;
            if(goodsFileSize > 500){
                throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,"【GoodsController：addGoods】上传的文件超出500KB大小，请重新上传......");
            }
        }

        try {
            int i = goodsService.addGoods(goods, goodsImage);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【GoodsController：addGoods】添加商品错误，清查看详细信息......");
        }
        return new Result<>(true, StatusCode.SC_OK,"添加商品成功！");
    }

    @PostMapping("/searchGoods")
    public Result searchGoods(@RequestBody Map paramBody){
        Result result = null;
        try {
            result = goodsService.searchGoods(paramBody);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【GoodsController：searchGoods】调用searchCenterFegin失败！......");
        }
        return result;
    }
    @Inner
    @PutMapping("/updateGoodsNumInventoryTimeTask")
    public Result updateGoodsNumInventoryTimeTask(@RequestBody Goods goods,
                                          @RequestParam String flag){
        return updateGoodsNumInventory(goods,flag);
    }


    @PutMapping("/updateGoodsNumInventory")
    public Result updateGoodsNumInventory(@RequestBody Goods goods,
                                          @RequestParam String flag){
        Result result = null;
        try {
            result = goodsService.updateGoodsNumInventory(goods, flag);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【GoodsController：updateGoodsNumInventory】调用updateGoodsNumInventory失败！......");
        }
        return result;
    }

    @Inner
    @PutMapping("/upateGoodsNumTimerTask")
    public Result upateGoodsNumTimerTask(@RequestBody Goods goods){
        return upateGoodsNum(goods);
    }

    @PreAuthorize("hasAnyRole('user')")
    @PutMapping("/updateGoodsNum")
    public Result upateGoodsNum(@RequestBody Goods goods){
        int i = 0;
        try {
             i = goodsService.updateGoods(goods);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【GoodsController：upateGoodsNum】修改失败！......");
        }
        return new Result<>(true, StatusCode.SC_OK,"修改商品成功！");
    }
}
