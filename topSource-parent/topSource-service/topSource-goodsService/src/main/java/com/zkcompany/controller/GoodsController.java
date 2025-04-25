package com.zkcompany.controller;
import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.Goods;
import com.zkcompany.service.GoodsService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
@RequestMapping("/goods")
@CrossOrigin
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

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
}
