package com.zkcompany.config;

import com.zkcompany.entity.Result;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.interceptor.AuthorizationGoodsFilter;
import com.zkcompany.interceptor.CustomHttpFirewallFilter;
import com.zkcompany.interceptor.InnerInterceptor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.Objects;

@Configuration
@Slf4j
public class WebGoodsSecurityConfig implements WebMvcConfigurer {

    @Value("${security.ignored}")
    private String[] ignoredUrls;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AuthorizationGoodsFilter authorizationGoodsFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InnerInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // 可以根据需要设置宽松的规则，例如允许特定的请求头字符等
        // 不禁止URL中的双斜杠，默认是禁止的
        firewall.setAllowUrlEncodedSlash(true);
        // 不禁止路径中的分号，默认是禁止的
        firewall.setAllowUrlEncodedPercent(true);

        // 设置请求头的规则，只允许非空值;
        firewall.setAllowedHeaderValues(value -> value!= null &&!value.isEmpty());
        return firewall;
    }


    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {
        // 获取 HttpFirewall 实例
        HttpFirewall firewall = httpFirewall();
        //前后端分离项目。需要关闭csrf。
        http.csrf(csrf -> csrf
                .disable()
        ).sessionManagement(session -> session
                //前后端分离项目，需要关闭session。
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ).authorizeHttpRequests(authorize -> authorize
                //所有请求都要被验证。
                .requestMatchers(ignoredUrls).permitAll()
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
                        Object message = redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityGoodsService_message).get();
                        if(Objects.isNull(message)){
                            message = "用户认证服务失败！请查看异常信息";
                        }else {
                            redisTemplate.delete(SystemConstants.redis_errorSecurityGoodsService_message);
                        }
                        response.setContentType("application/json;charset=UTF-8");
                        Result<Object> result = new Result<>(false, HttpStatus.UNAUTHORIZED.value(), message.toString(),authException.getLocalizedMessage());
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
        //自己定义的过滤器，主要对复杂的表头数据进行宽松的验证
        http.addFilterBefore(new CustomHttpFirewallFilter(firewall),UsernamePasswordAuthenticationFilter.class);
        //在过滤链中。加入自定义的过滤器。判断用户是否认证。
        http.addFilterBefore(authorizationGoodsFilter , UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
