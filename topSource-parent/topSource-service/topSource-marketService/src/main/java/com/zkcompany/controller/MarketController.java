package com.zkcompany.controller;

import com.zkcompany.annotation.Inner;
import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.*;
import com.zkcompany.service.MarketService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequestMapping("/market")
@RestController
@CrossOrigin
public class MarketController {

    @Autowired
    private MarketService marketService;

    @PreAuthorize("hasAnyRole('user','admin')")
    @PostMapping("/addMarketActivity")
    public Result addMarketActivity(
            @RequestPart("marketActivity") MarketActivity marketActivity,
            @RequestPart("activityGoodsList") List<ActivityGoods> activityGoodsList,
            @RequestPart("activityImage") MultipartFile activityImage){
        if(ObjectUtils.isEmpty(activityImage)){
            new BusinessException(StatusCode.SC_NOT_FOUND,"activityImage 参数为空！请传入activityImage参数");
        }else{
            long goodsFileSize = activityImage.getSize() / 1024;
            if(goodsFileSize > 500){
                throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,"【MarketController：addMarketActivity】上传的文件超出500KB大小，请重新上传......");
            }
        }
        String marketActivityId = null;
        try {
            marketActivityId = marketService.addMarketActivity(marketActivity, activityImage, activityGoodsList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Result(true, StatusCode.SC_OK,"添加活动成功！活动编号：" + marketActivityId);
    }

    @PreAuthorize("hasAnyRole('user','admin')")
    @GetMapping("/searchMarketActivity")
    public Result searchMarketActivity(@RequestBody Map searchParam){
        Map<String, Object> resultMap = null;
        try {
            resultMap = marketService.searchMarketActivity(searchParam);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【MarketController：searchMarketActivity】查询活动失败！......");
        }
        return new Result(true, StatusCode.SC_OK,"查询活动成功！.....",resultMap);
    }
    @Inner
    @PutMapping("/reduceActivityGoods")
    public Result reduceActivityGoods(@RequestBody ActivityGoods activityGoods){
        Result result = null;
        try {
            result = marketService.reduceActivityGoods(activityGoods);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【MarketController：reduceActivityGoods】减少活动商品库存发生错误......");
        }
        return result;
    }
    @Inner
    @PutMapping("/addActivityGoods")
    public Result addActivityGoods(@RequestBody ActivityGoods activityGoods){
        Result result = null;
        try {
            result = marketService.addActivityGoods(activityGoods);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【MarketController：addActivityGoods】增加活动商品库存失败......");
        }
        return result;
    }

    @Inner  //标注只能自己内部访问，外部请求不能访问
    @PostMapping("/addActivityStatus")
    public Result addActivityStatus(@RequestBody Order order){
        try {
            marketService.addActivityStatus(order);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【MarketController：addActivityStatus】添加抢单状态失败......");
        }
        return new Result(true, StatusCode.SC_OK,"添加抢单状态成功！.....");
    }

    @PreAuthorize("hasAnyRole('user','admin')")
    @GetMapping ("/searchActivityStatus")
    public Result searchActivityStatus(){
        List<ActivityStatus> activityStatusList = null;
        try {
            activityStatusList = marketService.searchActivityStatus(getUserid());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Result(true, StatusCode.SC_OK,"查询抢单状态成功！.....",activityStatusList);
    }


    private String getUserid(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }


}
