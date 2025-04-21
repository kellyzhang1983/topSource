package com.zkcompany.config;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenIntrospectionAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

@Configuration
public class WebOauthSercurityConfig {

    //http://localhost:9099/oauth2/authorize?client_id=zk&response_type=code&scope=topsource&redirect_uri=http://www.baidu.com
    //http://localhost:9099/oauth2/authorize?client_id=huawei&response_type=code&scope=app%20topsource&redirect_uri=http://huawei.com

    @Autowired
    private CustomJwtGeneratorAndDecoder customJwtGeneratorAndDecoder;

    @Autowired
    private CustomJwtRefreshTokenGenerator customJwtRefreshTokenGenerator;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    //自定义token生产规则，JWT编码以及解码
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator() {
        return new DelegatingOAuth2TokenGenerator(customJwtGeneratorAndDecoder,customJwtRefreshTokenGenerator);
    }



    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // 手动配置 OAuth2 授权服务器
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http
                // 只处理 /oauth2/** 路径的请求
                .securityMatcher("/oauth2/**")
                .with(authorizationServerConfigurer, Customizer.withDefaults()) // 应用 OAuth2 授权服务器配置
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(customJwtGeneratorAndDecoder))); // 启用 JWT 资源服务器


        // 显式配置 /oauth2/introspect 端点,解析自定义的JWT
        authorizationServerConfigurer
                .tokenIntrospectionEndpoint(tokenIntrospection -> tokenIntrospection
                        .introspectionResponseHandler(new AuthenticationSuccessHandler() {
                            @Override
                            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                // 获取 Token
                                String token = ((OAuth2TokenIntrospectionAuthenticationToken) authentication).getToken();

                                // 使用自定义的 JwtDecoder 解析 Token
                                Jwt jwt = null;
                                try {
                                    jwt = customJwtGeneratorAndDecoder.decode(token);
                                } catch (JwtException e) {
                                    response.setContentType("application/json;charset=UTF-8");
                                    Result<Object> result = new Result<>(false, StatusCode.ERROR, "解析token失败！请验证token有效性");
                                    response.getWriter().write(result.toJsonString(result));
                                    return;
                                }

                                // 将构建的 OAuth2TokenIntrospection 对象写入响应
                                response.setContentType("application/json;charset=UTF-8");
                                Result<Object> result = new Result<>(true, StatusCode.OK, "解析token成功！",jwt);
                                response.getWriter().write(result.toJsonString(result));
                            }
                        }));

        // 配置自定义的 JWT 生成器
        authorizationServerConfigurer
                .tokenGenerator(tokenGenerator())// 使用自定义的 JWT 生成器
                .oidc(Customizer.withDefaults()); // 启用 OpenID Connect 1.0

        // 配置异常处理
        http
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requestCache(requestCache -> requestCache.requestCache(requestCache()))
                .csrf(csrf -> csrf
                        .disable()
                )
                .authorizeHttpRequests(authorize -> authorize
                        //以下路径放行，不需要通过sercurity的验证
                        .requestMatchers("/oauth2/**","/userOAuth2/addClient").permitAll()
                        //其余任何请求都要经过认证。
                        .anyRequest().authenticated()

                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        /*.failureHandler((request, response, authentication) -> {
                            SavedRequest savedRequest = requestCache().getRequest(request, response);
                            response.sendRedirect("/login");
                        })*/
                        .successHandler((request, response, authentication) -> {
                            //http://localhost:9099/oauth2/authorize?client_id=huawei&response_type=code&scope=app%20topsource&redirect_uri=http://huawei.com
                            // 自定义重定向逻辑
                            StringBuffer url = new StringBuffer("/oauth2/authorize");
                            // 从 HttpSessionRequestCache缓存中获取原始请求
                            SavedRequest savedRequest = requestCache().getRequest(request, response);
                            if (savedRequest != null) {
                                //截取原始请求的参数？问号后面一串
                                String[] urlSplit = savedRequest.getRedirectUrl().split("error");
                                url.append(urlSplit[1]);
                                response.sendRedirect(url.toString());
                            }else {
                                // 如果没有原始请求，重定向到默认页面
                                response.sendRedirect("/login");
                            }
                        }));
                //.formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public HttpSessionRequestCache requestCache() {
        //自定义缓存逻辑
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null); // 禁用匹配参数
        return requestCache;
    }

}
