package com.zkcompany.service.impl;


import com.zkcompany.dao.MarketActivityMapper;
import com.zkcompany.entity.*;
import com.zkcompany.pojo.*;
import com.zkcompany.service.MarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MarketServiceImpl implements MarketService{

    @Autowired
    private MarketActivityMapper marketActivityMapper;

    @Override
    public void updateMarketActivityStatus(MarketActivity marketActivity) throws Exception {
        marketActivityMapper.updateMarketAcitivity(marketActivity.getActivityStatus(),marketActivity.getId());
    }



}
