package com.zkcompany.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.domain.LoginUser;
import com.zkcompany.entity.JwtUtil;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.User;
import com.zkcompany.service.UserAuthenticaitonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomJwtGeneratorAndDecoder implements OAuth2TokenGenerator<OAuth2Token> , JwtDecoder {
    @Autowired
    private RegisteredClientRepository registeredClientRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserAuthenticaitonService authenticaitonService;
    @Override
    public OAuth2Token generate(OAuth2TokenContext context) {
        if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
            LoginUser loginUser = null;
            Collection<? extends GrantedAuthority> authorities = null;
            String client_id = context.getRegisteredClient().getClientId();
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(client_id);
            //得到数据库中支持Oauth2.0的访问方式（授权码、客户端、刷新令牌）
            Set<AuthorizationGrantType> authorizationGrantTypes = registeredClient.getAuthorizationGrantTypes();
            //用户得到客户端生成令牌的访问类型
            AuthorizationGrantType grantType = context.getAuthorizationGrantType();
            //判断客户端是否支持这种类型
            if(authorizationGrantTypes.contains(grantType)) {
                //客户端模式：由于客户端模式只需要输入客户端的用户名和密钥，所以在内部强行指定一个用户；试用场景（公司内部使用）
                if (grantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
                    String username = "system_user";
                    loginUser = (LoginUser) authenticaitonService.loadUserByUsername(username);
                    loginUser.getUser();
                    authorities = loginUser.getAuthorities();

                } else if(grantType.equals(AuthorizationGrantType.AUTHORIZATION_CODE) || grantType.equals(AuthorizationGrantType.REFRESH_TOKEN)) {
                    loginUser = (LoginUser) context.getPrincipal().getPrincipal();
                    authorities = context.getPrincipal().getAuthorities();
                }
            }

            //获取用户信息
            User user = loginUser.getUser();

            // 获取用户权限信息
            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            //获取用户能访问的范围
            Object scopes = registeredClient.getScopes().toArray();
            //把用户信息、权限信息封装成MAP，然后通过JOSN进行序列化得到用户信息、权限信息字符串
            Map<String,Object> jwtMap = new HashMap<String,Object>();
            jwtMap.put("user",user);
            jwtMap.put("user_role", roles == null ? new ArrayList<String>(): roles);
            jwtMap.put("scopes", scopes);
            String jwtMap_jsonString = JSON.toJSONString(jwtMap);


            long accessTokenTimeToLive = registeredClient.getTokenSettings().getAccessTokenTimeToLive().toMillis();
            // 获取自定义的JWT
            String access_token = JwtUtil.createJWT(jwtMap_jsonString, accessTokenTimeToLive);
            //把token加入redis缓存中。如果注销，删除缓存中的token
            redisTemplate.boundHashOps(SystemConstants.redis_userToken).put(user.getUsername(),access_token);
            // 签发时间和过期时间
            Instant issuedAt = Instant.now(); // 签发时间
            Long expires_in = accessTokenTimeToLive / 1000; // 过期时间（秒数）


            // 返回OAuth2AccessToken
            return  new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                    access_token,
                    issuedAt,
                    Instant.now().plus(expires_in,ChronoUnit.SECONDS),
                    registeredClient.getScopes());
        }
        return null;
    }

    @Override
    public Jwt decode(String token) throws JwtException {

        // 使用自定义的 JWT 工具类解析 Token
        try {
            Map<String, Object> claims = JwtUtil.parseJWT(token);

            Object sub = claims.get("sub");

            JSONObject userObject = JSONObject.parseObject(sub.toString());


            // 从 claims 中提取用户信息和权限信息
            String userJson = userObject.get("user").toString();
            String rolesJson = userObject.get("user_role").toString();

            // 将用户信息和权限信息反序列化为对象
            User user = JSON.parseObject(userJson, User.class);
            List<String> roles = JSON.parseArray(rolesJson, String.class);


            // 构建 Spring Security 的 Jwt 对象
            return Jwt.withTokenValue(token)
                    .header("alg", "RS256") // 设置算法
                    .claim("user", user) // 自定义的用户信息
                    .claim("roles", roles) // 自定义的权限信息
                    .build();
        } catch (Exception e) {
            throw new JwtException("Failed to decode JWT", e);
        }
    }


}
