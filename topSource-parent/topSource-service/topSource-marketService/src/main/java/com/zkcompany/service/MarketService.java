package com.zkcompany.service;

import com.zkcompany.entity.Result;
import com.zkcompany.pojo.ActivityGoods;
import com.zkcompany.pojo.ActivityStatus;
import com.zkcompany.pojo.MarketActivity;
import com.zkcompany.pojo.Order;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface MarketService {

    String addMarketActivity(MarketActivity marketActivity, MultipartFile activityImage, List<ActivityGoods> activityGoodsList) throws Exception;

    Map<String, Object> searchMarketActivity(Map searchParam) throws Exception;

    Result reduceActivityGoods(ActivityGoods activityGoods) throws Exception;

    Result addActivityGoods(ActivityGoods activityGoods) throws Exception;

    void addActivityStatus(Order order) throws  Exception;

    List<ActivityStatus> searchActivityStatus(String userId) throws  Exception;
}
