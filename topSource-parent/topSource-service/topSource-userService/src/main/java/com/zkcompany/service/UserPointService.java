package com.zkcompany.service;

import com.zkcompany.pojo.Point;

import java.util.List;
import java.util.Map;

public interface UserPointService {

    void addUserPoint(Map<String,Object> body);

    List<Point> findUserPoint(String user_id);

    Integer findUserTotalPoint(String user_id);

    int cancelPonit(String user_id,String order_id);
}
