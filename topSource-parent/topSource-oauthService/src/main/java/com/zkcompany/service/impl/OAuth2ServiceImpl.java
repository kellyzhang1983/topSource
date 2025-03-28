package com.zkcompany.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class OAuth2ServiceImpl implements OAuth2Service {
    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Override
    public JSONObject addClientID(JSONObject body) throws RuntimeException{
        //组装后面的链接，原链接 //http://localhost:9099/oauth2/authorize?client_id=huawei&response_type=code&scope=app,topsource&redirect_uri=http://huawei.com
        //需要组装？后的链接，需要组装client_id=huawei&response_type=code&scope=app,topsource&redirect_uri=http://huawei.com返回给客户端
        StringBuffer url = new StringBuffer("?");
        //得到clientId
        String clientId = body.get("clientId") == null ? "" : body.get("clientId").toString();
        url.append("client_id=" + clientId + "&response_type=code");
        //客户端秘钥，用BCrypt对密码进行加密
        String password = body.get("password") == null ? "" : new BCryptPasswordEncoder().encode(body.get("password").toString());
        //得到客户端名称
        String clientName = body.get("clientName") == null ? "" : body.get("clientName").toString();
        //客户端过期时间，如果前端不设置，那么默认是30天过期
        //clientSecretExpiresAt(Instant.now().plus(clientSecretExpiresAt, ChronoUnit.DAYS))是按照天来进行设置
        long clientSecretExpiresAt = body.get("clientSecretExpiresAt") == null ? 30 : Long.valueOf(body.get("clientSecretExpiresAt").toString());
        //token设置：
        //accessTokenTimeToLive：token过期时间，默认是30天（43200分钟）
        //refreshTokenTimeToLive:刷新令牌时间过期，默认是90天（129600分钟）
        //authorizationCodeTimeToLive:授权码过期时间，默认是30分钟
        Duration accessTokenTimeToLive = body.getJSONObject("tokenSettings").get("accessTokenTimeToLive") == null ?
        Duration.ofMinutes(43200) :  Duration.ofMinutes(Long.valueOf(body.getJSONObject("tokenSettings").get("accessTokenTimeToLive").toString())) ;

        Duration refreshTokenTimeToLive = body.getJSONObject("tokenSettings").get("refreshTokenTimeToLive") == null ?
                Duration.ofMinutes(129600) :  Duration.ofMinutes(Long.valueOf(body.getJSONObject("tokenSettings").get("refreshTokenTimeToLive").toString())) ;

        Duration authorizationCodeTimeToLive = body.getJSONObject("tokenSettings").get("authorizationCodeTimeToLive") == null ?
                Duration.ofMinutes(30) :  Duration.ofMinutes(Long.valueOf(body.getJSONObject("tokenSettings").get("authorizationCodeTimeToLive").toString())) ;

        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(accessTokenTimeToLive)
                .refreshTokenTimeToLive(refreshTokenTimeToLive)
                .authorizationCodeTimeToLive(authorizationCodeTimeToLive)
                //token设置：自定义令牌格式，这里使用的是JWT
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                //token设置：允许使用令牌刷新
                .reuseRefreshTokens(true)
                .x509CertificateBoundAccessTokens(false)
                .build();
        //客户端设置
        ClientSettings clientSettings = ClientSettings.builder()
                //不允许弹出登录成功后的授权页面，而是直接返回授权码
                .requireAuthorizationConsent(false)
                .requireProofKey(false)
                .build();
        //设置客户端信息
        RegisteredClient registeredClient = RegisteredClient.withId(clientName)
                //客户端ID
                .clientId(clientId)
                //客户端秘钥，用BCrypt对密码进行加密
                .clientSecret(password)
                //客户端将使用Basic认证方式
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                //客户端的授权方式，封装成AuthorizationGrantType对象，然后放入SET集合中
                .authorizationGrantTypes(new Consumer<Set<AuthorizationGrantType>>() {
                    @Override
                    public void accept(Set<AuthorizationGrantType> authorizationGrantTypes) {
                        String authorizationGrantType = body.get("authorizationGrantTypes") == null ? "" : body.get("authorizationGrantTypes").toString();
                        String[] authorizationGrantSplit = authorizationGrantType.split(",");
                        for (int i = 0; i < authorizationGrantSplit.length; i++) {
                            switch (authorizationGrantSplit[i]){
                                case "authorization_code" :
                                    authorizationGrantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                                    break;
                                case "refresh_token" :
                                    authorizationGrantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
                                    break;
                                case "client_credentials" :
                                    authorizationGrantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
                                    break;
                            }
                        }
                    }
                })
                //客户端授权范围，如果有多个，放入SET集合中
                .scopes(new Consumer<Set<String>>() {
                    @Override
                    public void accept(Set<String> strings) {
                        String scopes = body.get("scopes") == null ? "" : body.get("scopes").toString();
                        String[] scopesSplit = scopes.split(",");
                        url.append("&scope=");
                        for (int i = 0; i < scopesSplit.length; i++) {
                            strings.add(scopesSplit[i]);
                            if(i == scopesSplit.length - 1){
                                url.append(scopesSplit[i]);
                            }else{
                                url.append(scopesSplit[i] + "%20");
                            }
                        }
                    }
                })
                //设置用户登出后的重定向 URI,如果有多个，放入SET结合中
                .redirectUris(new Consumer<Set<String>>() {
                    @Override
                    public void accept(Set<String> strings) {
                        String redirectUris = body.get("redirectUris") == null ? "" : body.get("redirectUris").toString();
                        String[] redirectUrisSplit = redirectUris.split(",");
                        url.append("&redirect_uri=");
                        for (int i = 0; i < redirectUrisSplit.length; i++) {
                            strings.add(redirectUrisSplit[i]);
                            if(i == redirectUrisSplit.length - 1){
                                url.append(redirectUrisSplit[i]);
                            }else{
                                url.append(redirectUrisSplit[i] + "%20");
                            }
                        }
                    }
                })
                //token设置，把tokenSettings对象进行设置
                .tokenSettings(tokenSettings)
                //客户端设置，把tclientSettings对象进行设置
                .clientSettings(clientSettings)
                //客户端申请时间，默认是现在的时间
                .clientIdIssuedAt(Instant.now())
                .clientName(clientName)
                //客户端过期时间，默认是30天
                .clientSecretExpiresAt(Instant.now().plus(clientSecretExpiresAt, ChronoUnit.DAYS))
                //设置客户端登出地址
                .postLogoutRedirectUris(new Consumer<Set<String>>() {
                    @Override
                    public void accept(Set<String> strings) {
                        String logoutRedirectUris = body.get("logoutRedirectUris") == null ? "" : body.get("logoutRedirectUris").toString();
                        String[] logoutRedirectUrisSplit = logoutRedirectUris.split(",");
                        for (int i = 0; i < logoutRedirectUrisSplit.length; i++) {
                            strings.add(logoutRedirectUrisSplit[i]);
                        }
                    }
                })
                .build();

        //利用oauth2自带的registeredClientRepository对象，将registeredClient对象存入数据库中，表名：oauth2_registered_client
        registeredClientRepository.save(registeredClient);
        //拼接URL地址放入JSON中，并返回给前端
        body.put("requestHttp",url.toString());
        return body;
    }
}
