package com.zkcompany.uitl;

import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.entity.DateUtil;
import com.zkcompany.entity.WorldTime;
import com.zkcompany.pojo.Goods;
import com.zkcompany.pojo.Order;
import com.zkcompany.pojo.Point;
import com.zkcompany.pojo.User;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

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
                point.setPointsChange(Integer.valueOf(value));
                break;
            case "change_type":
                point.setChangeType(Integer.valueOf(value));
                break;
            case "change_time":
                point.setChangeTime(DateUtil.formatStr(value,DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
        }
    }

    public static void convertOrder(Order order, String column, String value){
        switch (column){
            case "id":
                order.setId(value);
                break;
            case "user_id":
                order.setUserId(value);
                break;
            case "order_money":
                order.setOrderMoney(new BigDecimal(value));
                break;
            case "order_date" :
                order.setOrderDate(DateUtil.formatStr(value,DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                break;
            case "order_state" :
                order.setOrderState(value);
                break;
        }
    }

    public static void convertGoods(Goods goods, String column, String value){
        switch (column) {
            case "id":
                goods.setId(value);
                break;
            case "name":
                goods.setName(value);
                break;
            case "price":
                goods.setPrice(new BigDecimal(value));
                break;
            case "num":
                goods.setNum(Integer.parseInt(value));
                break;
            case "alert_num":
                goods.setAlertNum(Integer.parseInt(value));
                break;
            case "image":
                goods.setImage(value);
                break;
            case "weight":
                goods.setWeight(Integer.parseInt(value));
                break;
            case "create_time":
                goods.setCreateTime(DateUtil.formatStr(value, DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                break;
            case "update_time":
                goods.setUpdateTime(DateUtil.formatStr(value, DateUtil.PATTERN_YYYY_MM_DDHHMMSS));
                break;
            case "brand_name":
                goods.setBrandName(value);
                break;
            case "spec":
                if(!StringUtils.isEmpty(value)){
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    Object taste = jsonObject.get("口味");
                    if(!ObjectUtils.isEmpty(taste)){
                        goods.setTaste(taste.toString());
                    }

                    Object color = jsonObject.get("颜色");
                    if(!ObjectUtils.isEmpty(color)){
                        goods.setColor(color.toString());
                    }

                    Object version = jsonObject.get("版本");
                    if(!ObjectUtils.isEmpty(version)){
                        goods.setVersion(version.toString());
                    }

                    Object size_1 = jsonObject.get("尺寸");
                    if(!ObjectUtils.isEmpty(size_1)){
                        goods.setSize(size_1.toString());
                    }

                    Object size_2 = jsonObject.get("尺码");
                    if(!ObjectUtils.isEmpty(size_2)){
                        goods.setSize(size_2.toString());
                    }

                    Object size_3 = jsonObject.get("内存");
                    if(!ObjectUtils.isEmpty(size_3)){
                        goods.setSize(size_3.toString());
                    }

                    Object spec = jsonObject.get("规格");
                    if(!ObjectUtils.isEmpty(spec)){
                        goods.setSpec_search(spec.toString());
                    }
                }
                break;
            case "sale_num":
                goods.setSaleNum(Integer.parseInt(value));
                break;
            case "comment_num":
                goods.setCommentNum(Integer.parseInt(value));
                break;
            case "status":
                goods.setStatus(value);
                break;
            case "isAddWeight":
                goods.setIsAddWeight(value);
                break;
        }
    }




}