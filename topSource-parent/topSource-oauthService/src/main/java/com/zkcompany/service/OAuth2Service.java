package com.zkcompany.service;

import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

public interface OAuth2Service {

    JSONObject addClientID(JSONObject body) throws RuntimeException;
}
