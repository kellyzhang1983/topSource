package com.zkcompany.controller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.*;
import com.zkcompany.service.OrderService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequestMapping("/order")
@RestController
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;


    @GetMapping("/searchUser")
    public Result searchUser(@RequestParam(value = "id") String id) throws Exception {
        return  orderService.searchUser(id);
    }

    @PostMapping(value = "/createMarketOrder")
    @PreAuthorize("hasAnyRole('user','admin')")
    public Result createMarketOrder(@RequestBody Order order){
        return null;
    }

    @PostMapping(value = "/createOrder")
    @PreAuthorize("hasAnyRole('user','admin')")
    public Result createOrder(@RequestBody List<ShopCart> shopCarts){
        Result result = null;
        try {
            result = orderService.createOrder(getUserid(), shopCarts);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return  result;
    }

    @GetMapping(value = "/selectUserOrder")
    @PreAuthorize("hasAnyRole('user')")
    public Result selectUserOrder(){
        List<Order> orderList = null;
        try {
            orderList = orderService.selectUserOrder(getUserid());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return  new Result(true, StatusCode.SC_OK,"查询订单成功!",orderList);
    }

    @PreAuthorize("hasAnyRole('user')")
    @PutMapping("/payOrderStatus")
    public Result payOrderStatus(@RequestParam(value = "orderId") String orderId){
        int reslutCode = 0;
        try {
            reslutCode = orderService.payOrderStatus(orderId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (reslutCode){
            case 1 : return  new Result(true, StatusCode.SC_OK,"订单已经支付,订单编号：" + orderId);
            case 2: return  new Result(true, StatusCode.SC_OK,"订单支付成功,订单编号：" + orderId);
            case 3: return  new Result(true, StatusCode.SC_OK,"订单已关闭，不能支付！订单编号：" + orderId);
            case 4: return  new Result(true, StatusCode.SC_OK,"订单支付失败！订单编号：" + orderId);
            case 5: return  new Result(true, StatusCode.SC_OK,"订单已退款！订单编号：" + orderId);
            default: return  new Result(true, StatusCode.SC_OK,"未知定单，请联系客服处理,订单编号：" + orderId);
        }

    }
    @PreAuthorize("hasAnyRole('user')")
    @PutMapping("/refundOrder")
    public Result refundOrder(@RequestParam(value = "orderId") String orderId){
        int reslutCode = 0;
        try {
            reslutCode = orderService.refundOrder(orderId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (reslutCode){
            case 1 : return  new Result(true, StatusCode.SC_OK,"订单未完成支付、不能退款,订单编号：" + orderId);
            case 2: return  new Result(true, StatusCode.SC_OK,"订单退款成功,订单编号：" + orderId);
            case 3: return  new Result(true, StatusCode.SC_OK,"订单已关闭，不能发起退款！订单编号：" + orderId);
            case 4: return  new Result(true, StatusCode.SC_OK,"订单支付失败，无法退款！订单编号：" + orderId);
            case 5: return  new Result(true, StatusCode.SC_OK,"订单已退款！订单编号：" + orderId);
            default: return  new Result(true, StatusCode.SC_OK,"未知定单，请联系客服处理,订单编号：" + orderId);
        }
    }
    @PreAuthorize("hasAnyRole('user')")
    @PostMapping("/placeMarketOrder")
    @Bulkhead(name="orderServer_placeMarketOrder", fallbackMethod = "bulkheadError",type = Bulkhead.Type.SEMAPHORE)
    public Result placeMarketOrder(@RequestBody ActivityGoods activityGoods){
        Result result = null;
        try {
            result = orderService.placeMarketOrder(getUserid(),activityGoods);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return  result;
    }

    private String getUserid(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }



    public Result bulkheadError(Exception e) {
        return new Result<>(false, StatusCode.SC_TOO_MANY_REQUESTS,"userServer_Bulkhead：流量超出最大限制！系统繁忙，请稍后再试.....",e.getMessage());
    }
}
