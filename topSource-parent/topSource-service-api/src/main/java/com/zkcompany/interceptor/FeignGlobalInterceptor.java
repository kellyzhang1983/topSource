package com.zkcompany.interceptor;


import com.zkcompany.entity.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 动态fegin拦截器：
 * 1、正常从网关过来的请求：上下文attributes一定不为NULL；只需要判断在不在白名单即可
 * 2、MQ监听、定时任务如果要调用动态fegin，那么此时没有上下文路径，会执行：requestTemplate.header("timerTaskOrListenerMQ","true");各个微服务拦截器会放行
 */
@Configuration
public class FeignGlobalInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //如果是正常请求：RequestContextHolder.getRequestAttributes()不会为空，如果为空两种可能：1、微服务内部的定时服务。2、MQ监听后的处理。两者都不需要token
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            //如果是白名单、那么直接在header设置fegin-intereptor：true，各个微服务的拦截器会进行放行
            Boolean isWhite = request.getHeader("fegin-intereptor-whitelist") == null ? false : Boolean.valueOf(request.getHeader("fegin-intereptor-whitelist"));
            if(isWhite){
                requestTemplate.header("fegin-intereptor","true");
            }else {
                //正常从网关过来的请求，需要解析解析GATWAY_TOKEN
                String gatway_token = request.getHeader("GATWAY_TOKEN");
                //如果GATWAY_TOKEN为空，那么证明请求不是从网关过来，是Fegin接口调用。
                if(gatway_token != null){
                    requestTemplate.header("GATWAY_TOKEN", gatway_token);
                }else {
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null) {
                        String sub = "";
                        try {
                            //利用jwt工具类，解析token。
                            Claims claims = JwtUtil.parseJWT(authorization);
                            sub = claims.getSubject();
                        } catch (Exception e) {
                            throw new RuntimeException();
                        }
                        requestTemplate.header("GATWAY_TOKEN",sub);
                    }
                }
            }
        }else {
            //设置timerTaskOrListenerMQ：true.各个微服务会自动放行
            requestTemplate.header("timerTaskOrListenerMQ","true");
        }
    }
}
