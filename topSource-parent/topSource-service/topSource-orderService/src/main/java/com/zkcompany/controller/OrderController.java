package com.zkcompany.controller;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.User;
import com.zkcompany.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;


    @GetMapping("/searchUser")
    public Result searchUser(@RequestParam(value = "id") String id) throws Exception {
        return  orderService.searchUser(id);
    }

    @PreAuthorize("hasAnyRole('user')")
    @GetMapping("/addUserPoint")
    public Result addUserPoint() throws Exception {
        return  orderService.addUserPoint(getUserid());
    }

    @PreAuthorize("hasAnyRole('user')")
    @GetMapping("/paySatus")
    public Result paySatus(@RequestParam(value = "order_id") String order_id){
        int i = 0;
        try {
            Order order = new Order();
            order.setId(order_id);
            order.setUserId(getUserid());
            i = orderService.paySatus(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        switch (i){
            case 0:
                return new Result(true, StatusCode.SC_OK,"没找到订单，请检查订单编号！.....");
            case 1:
                return new Result(true, StatusCode.SC_OK,"支付订单成功！.....");
            case 2:
                return new Result(true, StatusCode.SC_OK,"订单已支付，请查看订单！.....");
            case 3:
                return new Result(true, StatusCode.SC_OK,"未支付或支付失败，订单已关闭！，.....");
            default:
                return new Result(true, StatusCode.SC_OK,"未知订单，请联系客服处理！.....");
        }

    }

    @GetMapping("/cancelOrder")
    public Result cancelOrder(@RequestParam(value = "order_id") String order_id){
        int result_i = 0;
        try {
            Order order = new Order();
            order.setId(order_id);

            result_i = orderService.cancelOrder(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(result_i == 0){
            return new Result(false, StatusCode.SC_NOT_FOUND,"不存在该订单，请检查订单编号！.....");
        }else{
            return new Result(true, StatusCode.SC_OK,"取消订单成功！.....");
        }


    }


    @GetMapping(value = "/placeOrder")
    public Result placeOrder(){
        String order_id = "";
        try {
            order_id = orderService.placeOrder(getUserid());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return  new Result(true, StatusCode.SC_OK,"正在努力抢单中，客官请耐心等待！订单编号：" + order_id,order_id);
    }

    @PreAuthorize("hasAnyRole('user','admin')")
    @GetMapping(value = "/searchOrder")
    public Result searchOrder(@RequestParam(value = "order_id") String order_id){
        Order order = null;
        try {
            order = orderService.searchOrder(order_id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(Objects.isNull(order)){
            return  new Result(true, StatusCode.SC_OK,"正在抢单中......请稍后查看订单信息！");
        }else {
            switch (order.getOrderState()){
                case "1":
                    return new Result(true, StatusCode.SC_OK, "抢单成功，请查看具体订单信息，请尽快支付！", order);
                case "2":
                    return new Result(true, StatusCode.SC_OK, "订单已完成支付，请查看物流信息！", order);
                case "3":
                    return new Result(true, StatusCode.SC_OK, "未支付或支付失败，订单已关闭，请重新下订单！", order);
                default:
                    return new Result(true, StatusCode.SC_OK, "未知订单，请联系客服处理！", order);
            }
        }

    }

    private String getUserid(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }



    public Result serviceError(Exception e){
        //System.out.println("name" + id);
        return new Result<>(false, StatusCode.REMOTEERROR,"order-server：服务器开小差了！，请稍后再试.....",e.getMessage());
    }
}
