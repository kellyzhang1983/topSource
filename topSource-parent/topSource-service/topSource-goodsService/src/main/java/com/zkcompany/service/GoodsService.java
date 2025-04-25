package com.zkcompany.service;

import com.zkcompany.entity.Result;
import com.zkcompany.pojo.Goods;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface GoodsService {

    int addGoods(Goods goods, MultipartFile file) throws Exception;

    Result searchGoods(Map paramBody) throws Exception;
}
