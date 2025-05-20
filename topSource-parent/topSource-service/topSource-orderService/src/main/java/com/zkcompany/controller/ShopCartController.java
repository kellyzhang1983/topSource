package com.zkcompany.controller;


import com.zkcompany.entity.BusinessException;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.ShopCart;
import com.zkcompany.pojo.User;
import com.zkcompany.service.ShopCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/shopCart")
public class ShopCartController {

    @Autowired
    private ShopCartService shopCartService;

    @PreAuthorize("hasAnyRole('user')")
    @RequestMapping("/addOrUpdateGoodsToShopCart")
    public Result addOrUpdateGoodsToShopCart(@RequestBody ShopCart shopCart,
                                             @RequestParam String operationMethod){
        ShopCart addGoodsToShopCart = null;
        try {
            addGoodsToShopCart = shopCartService.addOrUpdateGoodsToShopCart(getUserid(), shopCart,operationMethod);
        } catch (Exception e) {
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【orderServerError：ShopCartController】shopCartService.addGoodsToShopCart调取方法报错:！");
        }
        return new Result(true, StatusCode.SC_OK,"添加购物车成功！....",addGoodsToShopCart);
    }


    @PreAuthorize("hasAnyRole('user')")
    @RequestMapping("/userShopCart")
    public Result selectUserShopCart(){
        List<ShopCart> shopCarts = null;
        try {
            shopCarts = shopCartService.selectUserShopCart(getUserid());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(StatusCode.SC_INTERNAL_SERVER_ERROR,e.getMessage(),"【orderServerError：ShopCartController】shopCartService.selectUserShopCart调取方法报错:！");
        }
        return new Result(true, StatusCode.SC_OK,"查询用户购物车成功！....",shopCarts);
    }


    private String getUserid(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
