package com.zkcompany.uitl;

import com.zkcompany.entity.DateUtil;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.Point;
import com.zkcompany.pojo.User;

import java.math.BigDecimal;

public class ConvertObject {
    public static void convertUser(User user,String column,String value){
        switch (column){
            case "id":
                user.setId(value);
                break;
            case "username":
                user.setUsername(value);
                break;
            case "password":
                user.setPassword(value);
                break;
            case "ip":
                user.setIp(value);
                break;
            case "phone":
                user.setPhone(value);
                break;
            case "email":
                user.setEmail(value);
                break;
            case "created":
                user.setCreated(DateUtil.formatStr(value,DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                break;
            case "lastUpdate":
                user.setLastUpdate(DateUtil.formatStr(value,DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                break;
            case "source_type":
                user.setSource_type(value);
                break;
            case "nick_name":
                user.setNick_name(value);
                break;
            case "name":
                user.setName(value);
                break;
            case "status":
                user.setStatus(value);
                break;
            case "head_pic":
                user.setHead_pic(value);
                break;
            case "sex":
                user.setSex(value);
                break;
            case "last_logivalue":
                user.setLastUpdate(DateUtil.formatStr(value,DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                break;
        }
    }

    public static void convertPoint(Point point, String column, String value){
        switch (column){
            case "id":
                point.setId(value);
                break;
            case "user_id":
                point.setId(value);
                break;
            case "points_change":
                point.setPoints_change(Integer.valueOf(value));
                break;
            case "change_type":
                point.setChange_type(Integer.valueOf(value));
                break;
            case "change_time":
                point.setChange_time(DateUtil.formatStr(value,DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
        }
    }

    public static void convertOrder(Order order, String column, String value){
        switch (column){
            case "id":
                order.setId(value);
                break;
            case "user_id":
                order.setUser_id(value);
                break;
            case "order_money":
                order.setOrder_money(new BigDecimal(value));
                break;
            case "order_date" :
                order.setOrder_date(DateUtil.formatStr(value,DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                break;
            case "order_state" :
                order.setOrder_state(value);
                break;
        }
    }


}
