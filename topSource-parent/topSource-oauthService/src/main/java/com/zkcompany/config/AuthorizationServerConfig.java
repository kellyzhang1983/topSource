package com.zkcompany.config;

import com.zkcompany.service.impl.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;


@Configuration
@Slf4j
public class AuthorizationServerConfig {


    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        //设置oauth2的默认站点，可以不进行设置
        AuthorizationServerSettings authorizationServerSettings = AuthorizationServerSettings.builder()
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .jwkSetEndpoint("/oauth2/jwks")
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                .tokenRevocationEndpoint("/oauth2/revoke")
                .build();
        return authorizationServerSettings;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcOperations dataSource) {
        //从数据库中读取客户端信息
        return new JdbcRegisteredClientRepository(dataSource);
    }

    @Bean
    public UserDetailsService users() {
        //从数据库中读取客户信息
        return new UserDetailsServiceImpl();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(users());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }


    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                //设置访问令牌（Access Token）的有效时长。
                .accessTokenTimeToLive(Duration.ofDays(7))
                //用于设置刷新令牌（Refresh Token）的有效时长
                .refreshTokenTimeToLive(Duration.ofDays(3))
                //设置授权码（Authorization Code）的有效时长
                .authorizationCodeTimeToLive(Duration.ofMinutes(30))
                //用于设置是否允许重用刷新令牌
                .reuseRefreshTokens(true)
                //设置是否使用 X.509 证书来绑定访问令牌。
                .x509CertificateBoundAccessTokens(false)
                .build();
    }


}
