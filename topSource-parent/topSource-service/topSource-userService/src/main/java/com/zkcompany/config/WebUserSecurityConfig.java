package com.zkcompany.config;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.interceptor.AuthorizationUserFilter;
import com.zkcompany.interceptor.CustomHttpFirewallFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import java.io.IOException;
import java.util.Objects;

@Configuration
@Slf4j
public class WebUserSecurityConfig {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AuthorizationUserFilter authorizationUserFilter;

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // 可以根据需要设置宽松的规则，例如允许特定的请求头字符等
        // 不禁止URL中的双斜杠，默认是禁止的
        firewall.setAllowUrlEncodedSlash(true);
        // 不禁止路径中的分号，默认是禁止的
        firewall.setAllowUrlEncodedPercent(true);
        // 设置请求头的规则，只允许非空值;
        // 修改头部校验规则：允许特定头的空值
        firewall.setAllowedHeaderValues(value -> {
            // 放行GATWAY_TOKEN的任何值（保留您的复杂JSON头）
            if (value != null && value.startsWith("{")) return true;

            // 其他头保持原有非空校验
            return value != null && !value.isEmpty();
        });
        return firewall;
    }


    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {
        // 获取 HttpFirewall 实例
        HttpFirewall firewall = httpFirewall();
        http
                .setSharedObject(HttpFirewall.class, httpFirewall());
        //前后端分离项目。需要关闭csrf。
        http.csrf(csrf -> csrf
               .disable()
        ).sessionManagement(session -> session
                //前后端分离项目，需要关闭session。
               .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ).authorizeHttpRequests(authorize -> authorize
               //允许登录地址进行访问。
               //.requestMatchers("/user/**").permitAll()
                       .anyRequest()
                       .authenticated()
               );


        //自定义全局异常处理器。处理security框架抛出的异常
        http.exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {
            @Override
            public void customize(ExceptionHandlingConfigurer<HttpSecurity> httpSecurityExceptionHandlingConfigurer) {
                httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        //使用.getAndDelete()方法,Redis的服务器必须在6.2版本以上，目前Redis的服务器在5.0.14.1这个版本。
                        Object message = redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityUserService_message).get();
                        if(Objects.isNull(message)){
                            message = "用户认证服务失败！请查看异常信息";
                        }else {
                            redisTemplate.delete(SystemConstants.redis_errorSecurityUserService_message);
                        }
                        response.setContentType("application/json;charset=UTF-8");
                        Result<Object> result = new Result<>(false, StatusCode.SC_UNAUTHORIZED, message.toString(),authException.getLocalizedMessage());
                        response.getWriter().println(result.toJsonString(result));
                    }
                }).accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        response.setContentType("application/json;charset=UTF-8");
                        Result<Object> result = new Result<>(false, StatusCode.SC_FORBIDDEN, "你的权限不足！请查看异常信息",accessDeniedException.getLocalizedMessage());
                        response.getWriter().println(result.toJsonString(result));
                    }
                });
            }
        });

        http.addFilterBefore(new CustomHttpFirewallFilter(firewall),UsernamePasswordAuthenticationFilter.class);
        //在过滤链中。加入自定义的过滤器。判断用户是否认证。
        http.addFilterBefore(authorizationUserFilter , UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*@Bean
    public FilterRegistrationBean<Filter> emptyHeaderFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((request, response, chain) -> {
            if (((HttpServletRequest) request).getHeader("sw8-correlation") == null) {
                chain.doFilter(request, response);
            }
        });
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }*/
}
