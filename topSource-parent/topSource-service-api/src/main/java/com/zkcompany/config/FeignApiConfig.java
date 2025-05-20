package com.zkcompany.config;

import com.zkcompany.interceptor.FeignGlobalInterceptor;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignApiConfig {

    @Bean
    public FeignGlobalInterceptor feignGlobalInterceptor() {
        return new FeignGlobalInterceptor();
    }

    @Bean
    public RequestInterceptor internalSignInterceptor(InnerCallSignManager authManager) {

        return  new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                String sign = authManager.generateSign(requestTemplate.path());
                requestTemplate.header("InnerMethod-call", sign);
            }
        };
    }

}
