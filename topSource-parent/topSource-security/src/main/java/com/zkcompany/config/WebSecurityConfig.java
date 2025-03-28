package com.zkcompany.config;

import com.zkcompany.entity.IdWorker;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.StatusCode;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.filter.AuthorizationSecurityUserFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Objects;

@Configuration
@Slf4j
public class WebSecurityConfig{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AuthorizationSecurityUserFilter authorizationFilter;

   // @Bean(name="securityFilterChain")
    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {
        //前后端分离项目。需要关闭csrf。
       http.csrf(csrf -> csrf
               .disable()
        ).sessionManagement(session -> session
                //前后端分离项目，需要关闭session。
               .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/userAuthentication/login").permitAll()
               //其余任何请求都要经过认证。
               .anyRequest()
               .authenticated());

        //自定义全局异常处理器。处理security框架抛出的异常
        http.exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {
            @Override
            public void customize(ExceptionHandlingConfigurer<HttpSecurity> httpSecurityExceptionHandlingConfigurer) {
                httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        //自定义异常处理信息；
                        Object message = redisTemplate.boundValueOps(SystemConstants.redis_errorSecurity_message).get();
                        if(Objects.isNull(message)){
                            message = "用户认证服务失败！请查看异常信息";
                        }else{
                            redisTemplate.delete(SystemConstants.redis_errorSecurity_message);
                        }
                        response.setContentType("application/json;charset=UTF-8");
                        Result<Object> result = new Result<>(false, StatusCode.SC_UNAUTHORIZED, message.toString(),authException.getLocalizedMessage());
                        response.getWriter().println(result.toJsonString(result));
                    }
                }).accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        response.setContentType("application/json;charset=UTF-8");
                        Result<Object> result = new Result<>(false, HttpStatus.FORBIDDEN.value(), "你的权限不足！请查看异常信息",accessDeniedException.getLocalizedMessage());
                        response.getWriter().println(result.toJsonString(result));
                    }
                });
            }
        });

        //在过滤链中。加入自定义的过滤器。判断用户是否认证。
        http.addFilterBefore(authorizationFilter , UsernamePasswordAuthenticationFilter.class);


        http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                // 创建CorsConfiguration对象
                CorsConfiguration corsConfiguration = new CorsConfiguration();
                // 允许所有域名进行跨域调用
                corsConfiguration.addAllowedOriginPattern("*");
                // 允许任何请求头
                corsConfiguration.addAllowedHeader("*");
                // 允许任何方法（POST、GET、PUT、DELETE等）
                corsConfiguration.addAllowedMethod("*");
                // 允许携带凭证（如cookie）
                //corsConfiguration.setAllowCredentials(true);

                // 创建UrlBasedCorsConfigurationSource对象
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                // 对所有接口都有效
                source.registerCorsConfiguration("/**", corsConfiguration);
                // 将配置应用到CorsConfigurer
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,1);
    }




}
