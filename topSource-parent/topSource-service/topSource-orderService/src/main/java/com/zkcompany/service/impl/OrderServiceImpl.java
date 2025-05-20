package com.zkcompany.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zkcompany.dao.OrderGoodsMapper;
import com.zkcompany.dao.OrderMapper;
import com.zkcompany.dao.ShopCartMapper;
import com.zkcompany.entity.*;
import com.zkcompany.fegin.GoodsCenterFegin;
import com.zkcompany.fegin.MarketCenterFegin;
import com.zkcompany.fegin.RocketmqCenterFegin;
import com.zkcompany.fegin.UserCenterFegin;
import com.zkcompany.pojo.*;
import com.zkcompany.service.OrderService;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService, UserDetailsService {


    @Autowired
    private UserCenterFegin userCenterFegin;

    @Autowired
    private MarketCenterFegin marketCenterFegin;

    @Autowired
    private GoodsCenterFegin goodsCenterFegin;

    @Autowired
    private RocketmqCenterFegin rocketmqCenterFegin;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopCartMapper shopCartMapper;

    @Autowired
    private OrderGoodsMapper orderGoodsMapper;

    @Autowired
    private IdWorker idCreate;

    @Autowired
    private RedisTemplate redisTemplate;



    @Override
    public Result<User> searchUser(String id) throws Exception {
        return userCenterFegin.findUser(id);
    }

    /**
     * 生成普通订单
     * ***/
    @Override
    @GlobalTransactional
    public Result createOrder(String userId, List<ShopCart> shopCarts) throws Exception {
        List<String> messageList = new ArrayList<String>();
        String message = "";
        //1、查询用户所有的购物车商品
        List<ShopCart> shopCartList = (List<ShopCart>)redisTemplate.boundHashOps(SystemConstants.redis_shopCartUser).get(userId);
        //1.1、如果购物车没有商品，那么抛出异常
        if(ObjectUtils.isEmpty(shopCartList)){
            return new Result(false,StatusCode.SC_NOT_FOUND,"用户：" + userId + "的购物车没有对应商品，不能生成订单！");
        }

        //2、查询用户所有的购物车商品是否存在，存在就加入订单，如果不存在就记录异常信息，并提示前端，商品不存在或已下架
        BigDecimal goodsTotalPrice = new BigDecimal(0);
        for(ShopCart shopCart_selected : shopCarts){
            Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(shopCart_selected.getGoodsId());
            if(ObjectUtils.isEmpty(goods)){
                message =  "商品名称：" + goods.getName() + "已下架，订单已发生变化，请关注！";
                messageList.add(message);
                shopCartMapper.deleteByPrimaryKey(shopCart_selected);
            } else {
                //2.1、计算选中商品的总价！
                goodsTotalPrice = goodsTotalPrice.add(goods.getPrice().multiply(new BigDecimal(shopCart_selected.getGoodsNum())));
            }

        }
        //2.2、如果用户在购物车选择的商品都已删除或者下架，那么直接提示前端无法生产订单！
        if(goodsTotalPrice.compareTo(BigDecimal.ZERO) == 0){
            return new Result(false,StatusCode.SC_NOT_FOUND,"用户：" + userId + "的购物车对应商品发生异常，不能生成订单！");
        }

        String orderId = String.valueOf(idCreate.nextId());
        int count = 0;
        //3、生成订单详细商品
        for(ShopCart shopCart_selected : shopCarts) {
            //获取该用户在商品列表里的商品；
            Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(shopCart_selected.getGoodsId());
            OrderGoods orderGoods = new OrderGoods();
            //购物车的商品在商品列表中不为空，那么才构建orderGoods对象
            if(!ObjectUtils.isEmpty(goods)){
                //判断购物车选中的商品数量是否小于等于库存商品数量
                boolean flag = true;
                if(orderGoods.getGoodsNum() - shopCart_selected.getGoodsNum() <= 0 ){
                    message =  "商品名称：（" + goods.getName() + "）库存不足，无法购买！";
                    messageList.add(message);
                    flag = false;
                    count ++;
                }
                if(flag){
                    orderGoods.setId(String.valueOf(idCreate.nextId()));
                    orderGoods.setOrderId(orderId);
                    orderGoods.setGoodsId(shopCart_selected.getGoodsId());
                    orderGoods.setGoodsName(goods.getName());
                    orderGoods.setBrandName(goods.getBrandName());
                    orderGoods.setGoodsImage(goods.getImage());
                    orderGoods.setGoodsNum(shopCart_selected.getGoodsNum());
                    orderGoods.setPrice(goods.getPrice());
                    orderGoods.setTotalPrice(goods.getPrice().multiply(new BigDecimal(shopCart_selected.getGoodsNum())));
                    orderGoods.setCreated(WorldTime.chinese_time(new Date()));
                    orderGoods.setUpdated(WorldTime.chinese_time(new Date()));
                    orderGoodsMapper.insertSelective(orderGoods);

                    //生成订单商品后清理购物车
                    for(ShopCart shopCart : shopCartList){
                        if(shopCart.getGoodsId().equals(shopCart_selected.getGoodsId())){
                            //判断生成订单时商品的数量
                            if(shopCart.getGoodsNum() - shopCart_selected.getGoodsNum() <= 0){
                                //如果生成订单时，商品的数量等于购物车订单的数量，直接删除商品
                                shopCartMapper.deleteByPrimaryKey(shopCart);
                            }else{
                                //如果生成订单时，选择商品的数量小于购物车订单的数量，修改购物车的数量以及对应的总价
                                shopCart.setGoodsNum(shopCart.getGoodsNum() - shopCart_selected.getGoodsNum());
                                shopCart.setTotalPrice(shopCart.getPrice().multiply(new BigDecimal(shopCart.getGoodsNum() - shopCart_selected.getGoodsNum())));
                                shopCartMapper.updateByPrimaryKeySelective(shopCart);
                            }

                        }
                    }
                    //在商品列表中减少库存
                    //goods.setSaleNum(goods.getSaleNum() + orderGoods.getGoodsNum());
                    goods.setNum(goods.getNum() - orderGoods.getGoodsNum());
                    Result result = goodsCenterFegin.upateGoodsNum(goods);
                    if(!result.isFlag()){
                        redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityOrderService_message).set("调用goodsCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                        throw new RuntimeException("调用goodsCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                    }
                }
            }
        }

        //4、如果所有购物车的商品都库存不足，则无法生成订单
        if(shopCarts.size() == count){
            return new Result(false,StatusCode.SC_INTERNAL_SERVER_ERROR,"用户：" + userId + "的购物车所有商品库存不足，不能生成订单！");
        }

        //5、生成订单
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setOrderDate(WorldTime.chinese_time(new Date()));
        order.setOrderMoney(goodsTotalPrice);
        order.setOrderState("1");
        int reuslt = orderMapper.insertSelective(order);
        //生成订单增加积分
        if(reuslt > 0){
            Map<String, Object> point = create_point(order,200);
            Result result = userCenterFegin.addUserPoint(point);
            if(!result.isFlag()){
                redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityOrderService_message).set("调用userCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
            }
        }

        return new Result(true,StatusCode.SC_OK,"订单生成成功！订单编号：" + orderId,messageList);
    }
    /**
     * 下定营销产品
     * ***/
    @Override
    public Result placeMarketOrder(String userId, ActivityGoods activityGoods) throws Exception {
        MarketActivity marketActivity = (MarketActivity)redisTemplate.boundHashOps(SystemConstants.redis_marketActivity).get(activityGoods.getMarketId());
        //判断营销活动是否具有该商品
        if(ObjectUtils.isEmpty(marketActivity)){
            return new Result(false,StatusCode.SC_NOT_FOUND,"没有该营销活动，营销活动编号：" + activityGoods.getMarketId() + "！");
        }
        //判断营销活动状态是否是正常开始 0、未开始。1、开始、2、已结束
        if(marketActivity.getActivityStatus().equals("0")){
            return new Result(false,StatusCode.SC_MULTI_STATUS,"该营销活动还没开始，不能下单，营销活动编号：" + activityGoods.getMarketId() + "！");
        }

        if(marketActivity.getActivityStatus().equals("2")){
            return new Result(false,StatusCode.SC_MULTI_STATUS,"该营销活动已经结束，不能下单，营销活动编号：" + activityGoods.getMarketId() + "！");
        }
        //获取营销活动商品
        List<ActivityGoods> activityGoodsList =  (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(marketActivity.getId());

        String goodsId = "";
        String marketId = "";
        BigDecimal price = new BigDecimal(0);
        boolean flag = true;
        //如果营销活动订单单价发生变化，重新计算定单价格
        for(ActivityGoods activityGoods_1 : activityGoodsList){
            if(activityGoods_1.getGoodsId().equals(activityGoods.getGoodsId())){
                goodsId = activityGoods_1.getGoodsId();
                price = price.add(activityGoods_1.getPrice());
                marketId = activityGoods_1.getMarketId();
                flag = false;
                break;
            }
        }
        //如果选择的商品不在营销活动内，则报错
        if(flag){
            return new Result(false,StatusCode.SC_INTERNAL_SERVER_ERROR,"营销活动：" + activityGoods.getMarketId() + "中没有对应的商品，不能生成订单！");
        }

        //添加订单信息
        String orderId = String.valueOf(idCreate.nextId());
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setOrderDate(WorldTime.chinese_time(new Date()));
        order.setOrderMoney(price);
        order.setOrderActivity(marketId);
        order.setOrderState("1");
        order.setGoodsId(goodsId);
        //订单信息JSON格式化
        //JSONObject josn_order = (JSONObject)JSONObject.toJSON(order);
        //sendMessageMQ.SendMessage_async(order,RocketMQInfo.rocketMQ_topic_orderProcess,josn_order);
        //MQ发送订单信息
        Result result = rocketmqCenterFegin.orderSendMessage(order);
        if(!result.isFlag()){
            throw new RuntimeException("调用rocketmqCenterFegin接口失败！请查看详细原因：" + result.getMessage());
        }
        //sendMessageMQ.SendMessage_async(order,RocketMQInfo.rocketMQ_topic_orderProcess,josn_order);
        return new Result(true,StatusCode.SC_OK,"客官！您正在抢单中，请稍后查询是否抢单成功！订单编号：" + orderId);
    }

    /**
     * 查询生成的订单（包含普通订单和营销订单）
     * ***/
    @Override
    public List<Order> selectUserOrder(String userId) throws Exception {
        //1、得到该用户所有的订单
        List<Order> userOrder = (List<Order>)redisTemplate.boundHashOps(SystemConstants.redis_userOrder).get(userId);
        if(ObjectUtils.isEmpty(userOrder)){
            userOrder = new ArrayList<>();
            return userOrder;
        }

        for(Order order : userOrder){
            List<OrderGoods> newOrderGoodsList = new ArrayList<OrderGoods>();
            BigDecimal total = new BigDecimal(0);
            //2、得到该订单下所有对应的商品
            List<OrderGoods> orderGoodsList = (List<OrderGoods>)redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).get(order.getId());
            if(!ObjectUtils.isEmpty(orderGoodsList)){
                for(OrderGoods orderGoods : orderGoodsList){
                    Goods goods = (Goods)redisTemplate.boundHashOps(SystemConstants.redis_goods).get(orderGoods.getGoodsId());
                    //判断未支付订单和已支付订单、关闭订单和退款订单，查询商品价格是否变动
                    if(!order.getOrderState().equals("1")){
                        //普通商品和营销商品已支付订单价格不用变动，不用从商品列表中获取最新价格，只需要在订单商品表中获取价格
                        if(StringUtils.isEmpty(order.getOrderActivity())){
                            total = total.add(orderGoods.getTotalPrice());
                            newOrderGoodsList.add(orderGoods);
                        }else{
                            List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(order.getOrderActivity());
                            for(ActivityGoods activityGoods : activityGoodsList){
                                if (activityGoods.getGoodsId().equals(orderGoods.getGoodsId())) {
                                    total = total.add(orderGoods.getPrice());
                                    newOrderGoodsList.add(orderGoods);
                                }
                            }
                        }

                    }else {
                        //判断是普通订单还是营销订单
                        if(StringUtils.isEmpty(order.getOrderActivity())) {
                            //普通订单需要从商品列表中查询价格，重新计算订单总价
                            if(goods.getPrice().compareTo(orderGoods.getPrice()) != 0){
                                orderGoods.setPrice(goods.getPrice());
                                orderGoods.setTotalPrice(goods.getPrice().multiply(new BigDecimal(orderGoods.getGoodsNum())));
                                total = total.add(orderGoods.getTotalPrice());
                                newOrderGoodsList.add(orderGoods);//重新计算商品价格total = total.add(goods.getPrice());
                            }else{
                                total = total.add(orderGoods.getTotalPrice());
                                newOrderGoodsList.add(orderGoods);
                            }
                        }else{
                            List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(order.getOrderActivity());
                            for(ActivityGoods activityGoods : activityGoodsList){
                                //营销订单需要从营销活动的商品列表中查询价格，并重新计算订单总价
                                if (activityGoods.getGoodsId().equals(orderGoods.getGoodsId())) {
                                    if(activityGoods.getPrice().compareTo(orderGoods.getPrice()) != 0){
                                        //如果价格有变动，那么会去营销活动中商品最新的价格
                                        orderGoods.setPrice(activityGoods.getPrice());
                                        orderGoods.setTotalPrice(activityGoods.getPrice().multiply(new BigDecimal(orderGoods.getGoodsNum())));
                                        total = total.add(activityGoods.getPrice());
                                        newOrderGoodsList.add(orderGoods);
                                    }else{
                                        total = total.add(activityGoods.getPrice());
                                        newOrderGoodsList.add(orderGoods);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //重新计算订单总价
            order.setOrderMoney(total);
            orderMapper.updateByPrimaryKeySelective(order);
            order.setOrderGoodsList(newOrderGoodsList);
        }
        return userOrder;
    }
    /**
     * 订单支付
     * ***/
    @Override
    @GlobalTransactional
    public int payOrderStatus(String orderId) throws Exception {
        int resultCode = 0;
        //1、查询需要支付的订单
        Order order = (Order)redisTemplate.boundHashOps(SystemConstants.redis_Order).get(orderId);
        if(ObjectUtils.isEmpty(order)){
            resultCode = 6;
            return resultCode;
        }
        //2、该订单状态为1（未支付）订单才能支付
        if(order.getOrderState().equals("1")){
            order.setOrderState("2");
            //根据定单ID修改订单状态
            int reuslt = orderMapper.upateOrderStatus(order.getId(),order.getOrderState());
            if(reuslt > 0){
                //调用userCenterFegin增加订单积分
                Map<String, Object> point = create_point(order,500);
                Result result = userCenterFegin.addUserPoint(point);
                if(!result.isFlag()){
                    redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityOrderService_message).set("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                    throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                }
            }

            //2.查询该订单下的商品集合
            List<OrderGoods> orderGoodsList =(List<OrderGoods>) redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).get(orderId);
            for(OrderGoods orderGoods : orderGoodsList){
                //找到该商品，增加该商品的销售数量
                Goods goods = (Goods)redisTemplate.boundHashOps(SystemConstants.redis_goods).get(orderGoods.getGoodsId());
                goods.setSaleNum(goods.getSaleNum() + orderGoods.getGoodsNum());
                Result result = goodsCenterFegin.upateGoodsNumTimerTask(goods);
                if(!result.isFlag()){
                    redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityOrderService_message).set("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                    throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                }

            }
            resultCode = 2;
            return resultCode;
        }else{
            resultCode = Integer.valueOf(order.getOrderState());
            return resultCode;
        }
    }

    @GlobalTransactional
    @Override
    public int refundOrder(String orderId) throws Exception{
        int resultCode = 0;
        //1、查詢此定单
        Order order = (Order)redisTemplate.boundHashOps(SystemConstants.redis_Order).get(orderId);
        if(ObjectUtils.isEmpty(order)){
            resultCode = 6;
            return resultCode;
        }
        if(order.getOrderState().equals("2")){
            order.setOrderState("5");
            order.setOrderDate(WorldTime.chinese_time(order.getOrderDate()));
            int reuslt = orderMapper.updateByPrimaryKeySelective(order);
            if(reuslt > 0){
                //1.3调用userCenterFegin增加订单积分
                Map<String, Object> point = create_point(order,-500);
                Result result = userCenterFegin.addUserPoint(point);
                if(!result.isFlag()){
                    redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityOrderService_message).set("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                    throw new RuntimeException("调用userCenter接口失败！请查看详细原因：" + result.getMessage());
                }
            }

            //2.回退商品库存
            try {
                fallbackGoodNum(order);
            } catch (Exception e) {
                throw new RuntimeException("调用fallbackGoodNum回退商品库存失败！请查看详细原因：" + e.getMessage());
            }

            //3.回退商品销量
            try {
                fallbackGoodSaleNum(order);
            } catch (Exception e) {
                throw new RuntimeException("调用fallbackGoodSaleNum回退商品销量失败！请查看详细原因：" + e.getMessage());
            }
            resultCode = 2;
            return resultCode;
        }else{
            resultCode = Integer.valueOf(order.getOrderState()) ;
            return resultCode;
        }
    }

    public void fallbackGoodNum(Order order) throws Exception{
        List<OrderGoods> orderGoodsList =(List<OrderGoods>) redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).get(order.getId());
            for(OrderGoods orderGoods : orderGoodsList) {
                Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(orderGoods.getGoodsId());
                if (StringUtils.isEmpty(order.getOrderActivity())) {
                    goods.setNum(orderGoods.getGoodsNum());
                    Result result = goodsCenterFegin.updateGoodsNumInventoryTimeTask(goods,"add");
                    if (!result.isFlag()) {
                        throw new RuntimeException("调用goodsCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                    }
                }else{
                    List<ActivityGoods> activityGoodsList = (List<ActivityGoods>)redisTemplate.boundHashOps(SystemConstants.redis_marketActivityGoods).get(order.getOrderActivity());
                    for(ActivityGoods activityGoods : activityGoodsList){
                        if(activityGoods.getGoodsId().equals(orderGoods.getGoodsId())){
                            Result result = marketCenterFegin.addActivityGoods(activityGoods);
                            if(!result.isFlag()){
                                throw new RuntimeException("调用marketCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                            }

                        }
                    }
                }
            }
        //}
    }

    public void fallbackGoodSaleNum(Order order) throws Exception{
        List<OrderGoods> orderGoodsList =(List<OrderGoods>) redisTemplate.boundHashOps(SystemConstants.redis_orderGoods).get(order.getId());
        if(StringUtils.isEmpty(order.getOrderActivity())) {
            for (OrderGoods orderGoods : orderGoodsList) {
                Goods goods = (Goods) redisTemplate.boundHashOps(SystemConstants.redis_goods).get(orderGoods.getGoodsId());
                goods.setSaleNum(goods.getSaleNum() - orderGoods.getGoodsNum());
                Result result = goodsCenterFegin.upateGoodsNumTimerTask(goods);
                if (!result.isFlag()) {
                    throw new RuntimeException("调用goodsCenterFegin接口失败！请查看详细原因：" + result.getMessage());
                }
            }
        }

    }


    private Map<String,Object> create_point(Order order,Integer point){
        Map<String,Object> body = new HashMap<String,Object>();
        body.put("user_id",order.getUserId());
        body.put("change_type",3);
        body.put("points_detail",order.getId());
        body.put("point",point);
        return body;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

}
