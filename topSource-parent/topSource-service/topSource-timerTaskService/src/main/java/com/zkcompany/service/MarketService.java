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

    void updateMarketActivityStatus(MarketActivity marketActivity) throws Exception;
}
