package com.zkcompany.service;

import com.zkcompany.entity.Result;
import com.zkcompany.pojo.Goods;
import com.zkcompany.pojo.Order;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface GoodsService {

    int addGoods(Goods goods, MultipartFile file) throws Exception;

    Result searchGoods(Map paramBody) throws Exception;

    int updateGoods(Goods good) throws Exception;

    Result updateGoodsNumInventory(Goods goods, String flag) throws Exception;


}
