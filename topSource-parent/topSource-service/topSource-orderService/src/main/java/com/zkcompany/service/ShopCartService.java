package com.zkcompany.service;

import com.zkcompany.pojo.ShopCart;

import java.util.List;

public interface ShopCartService {

    ShopCart addOrUpdateGoodsToShopCart(String userId,ShopCart shopCart,String operationMethod) throws Exception;

    List<ShopCart> selectUserShopCart(String userId) throws Exception;
}
