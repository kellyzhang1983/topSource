package com.zkcompany.config;

import com.alibaba.fastjson2.JSON;
import com.zkcompany.domain.LoginUser;
import com.zkcompany.entity.JwtUtil;
import com.zkcompany.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
@Component
public class CustomJwtRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2Token> {
    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Override
    public OAuth2Token generate(OAuth2TokenContext context) {
        if (context.getTokenType() == OAuth2TokenType.REFRESH_TOKEN) {
            String client_id = context.getRegisteredClient().getClientId();
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(client_id);
            long refreshTokenTimeToLive = registeredClient.getTokenSettings().getRefreshTokenTimeToLive().toMillis();
            // 获取自定义的JWT
            String refresh_token = JwtUtil.createJWT("", refreshTokenTimeToLive);

            // 签发时间和过期时间
            Instant issuedAt = Instant.now(); // 签发时间
            Long expires_in = refreshTokenTimeToLive / 1000; // 过期时间（秒数）

            return new OAuth2RefreshToken(refresh_token,issuedAt,Instant.now().plus(expires_in, ChronoUnit.SECONDS));
        }
        return null;
    }
}
