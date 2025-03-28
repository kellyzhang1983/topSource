package com.zkcompany.config;

import com.zkcompany.interceptor.FeignUserInterceptor;
import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public FeignUserInterceptor feignUserInterceptor() {
        return new FeignUserInterceptor();
    }

}
