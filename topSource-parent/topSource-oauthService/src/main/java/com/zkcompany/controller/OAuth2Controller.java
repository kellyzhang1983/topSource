package com.zkcompany.controller;


import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.service.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/userOAuth2")
public class OAuth2Controller {

    @Value("${server.port}")
    private String port;

    @Autowired
    private OAuth2Service oAuth2Service;


    @PostMapping("/addClient")
    public Result addClientID(@RequestBody JSONObject body,HttpServletRequest request){
        JSONObject jsonObject = null;
        try {
            Object clientId = body.get("clientId");
            if (ObjectUtils.isEmpty(clientId)){
                return new Result(false, StatusCode.ERROR,"请添加clientId！");
            }

            Object password = body.get("password");
            if (ObjectUtils.isEmpty(password)){
                return new Result(false, StatusCode.ERROR,"请添加password！");
            }

            Object clientName = body.get("clientName");
            if (ObjectUtils.isEmpty(clientName)){
                return new Result(false, StatusCode.ERROR,"请添加clientName！");
            }

            Object authorizationGrantType = body.get("authorizationGrantTypes");
            if (ObjectUtils.isEmpty(authorizationGrantType)){
                return new Result(false, StatusCode.ERROR,"请添加authorizationGrantTypes！");
            }

            Object redirectUris = body.get("redirectUris");
            if (ObjectUtils.isEmpty(redirectUris)){
                return new Result(false, StatusCode.ERROR,"请添加redirectUris！");
            }

            Object logoutRedirectUris = body.get("logoutRedirectUris");
            if (ObjectUtils.isEmpty(logoutRedirectUris)){
                return new Result(false, StatusCode.ERROR,"请添加logoutRedirectUris！");
            }

            Object scopes = body.get("scopes");
            if (ObjectUtils.isEmpty(scopes)){
                return new Result(false, StatusCode.ERROR,"请添加scopes！");
            }

            Object tokenSettings = body.get("tokenSettings");
            if (ObjectUtils.isEmpty(tokenSettings)){
                return new Result(false, StatusCode.ERROR,"请添加tokenSettings！");
            }

            Object accessTokenTimeToLive = body.getJSONObject("tokenSettings").get("accessTokenTimeToLive");
            if (ObjectUtils.isEmpty(accessTokenTimeToLive)){
                return new Result(false, StatusCode.ERROR,"请添加accessTokenTimeToLive！");
            }

            Object refreshTokenTimeToLive = body.getJSONObject("tokenSettings").get("refreshTokenTimeToLive");
            if (ObjectUtils.isEmpty(refreshTokenTimeToLive)){
                return new Result(false, StatusCode.ERROR,"请添加refreshTokenTimeToLive！");
            }

            Object authorizationCodeTimeToLive = body.getJSONObject("tokenSettings").get("authorizationCodeTimeToLive");
            if (ObjectUtils.isEmpty(authorizationCodeTimeToLive)){
                return new Result(false, StatusCode.ERROR,"请添加authorizationCodeTimeToLive！");
            }
            jsonObject = oAuth2Service.addClientID(body);
            String ip = "http://" + request.getLocalAddr()  + ":" + port + "/oauth2/authorize";
            jsonObject.replace("requestHttp",jsonObject.get("requestHttp"),ip + jsonObject.get("requestHttp"));
        } catch (RuntimeException e) {
            return new Result(false, StatusCode.ERROR,"添加客户端信息失败！请看详细信息",e.getMessage());
        }
        return new Result(true, StatusCode.OK,"添加客户端信息成功！",jsonObject);
    }
}
