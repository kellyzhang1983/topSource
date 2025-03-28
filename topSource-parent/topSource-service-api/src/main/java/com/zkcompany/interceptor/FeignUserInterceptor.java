package com.zkcompany.interceptor;


import com.zkcompany.entity.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Configuration
public class FeignUserInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            //从网关过来的请求，需要解析解析GATWAY_TOKEN
            String gatway_token = request.getHeader("GATWAY_TOKEN");
            //如果GATWAY_TOKEN为空，那么证明请求不是从网关过来，是走的服务调用。
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
    }
}
