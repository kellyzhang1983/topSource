package com.zkcompany.entity;

/**
 * 描述
 *
 * @author zk
 * @version 1.0
 * @package entity *
 * @since 1.0
 */
public class SystemConstants {
    /**
     * User对象存放到Redis的key
     */
    public static final String redis_userInfo = "UserInfo";
    public static final String redis_userInfo_key = "UserInfo_";
    public static final String redis_userPoint =  "UserPoint";
    public static final String redis_userPoint_key =  "UserPoint_";

    public static final String redis_userOrder = "userOrder";
    public static final String redis_Order = "Order";
    public static final String redis_userOrder_key = "userOrder_";

    public static final String redis_goods = "Goods";
    public static final String redis_orderGoods = "orderGoods";
    //public static final String redis_orderGoodsUser = "orderGoodsUser";
    public static final String redis_shopCartList = "shopCartList";
    public static final String redis_shopCartUser = "shopCartUser";

    public static final String redis_marketActivity = "marketActivity";
    public static final String redis_marketActivityGoods = "marketActivityGoods";

    public static final String redis_activityGoodsNum = "activityGoodsNum";
    public static final String redis_activityStatus = "activityStatus";
    public static final String redis_userActivityStatus = "userActivityStatus";

    public static final String redis_userRoleAndPermission = "userRoleAndPermission";
    public static final String redis_userRoleAndPermission_key = "userRoleAndPermission_";

    public static final String redis_userToken = "userToken";

    public static final String redis_errorSecuritySearchService_message = "redis_errorSecuritySearchService_message";
    public static final String redis_errorSecurityGoodsService_message = "redis_errorSecurityGoodsService_message";
    public static final String redis_errorSecurityOrderService_message = "redis_errorSecurityOrderService_message";
    public static final String redis_errorSecurityUserService_message = "redis_errorSecurityUserService_message";
    public static final String redis_errorSecurityMarketService_message = "redis_errorSecurityMarketService_message";
    public static final String redis_errorSecurity_message = "redis_errorSecurity_message";

}
