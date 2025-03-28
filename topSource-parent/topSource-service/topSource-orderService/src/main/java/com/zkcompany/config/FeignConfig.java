package com.zkcompany.config;

import com.zkcompany.interceptor.FeignUserInterceptor;
import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Retryer customRetryer(){
        //开启fegin接口的重试机制，自定义初始间隔时间，重试间隔时间，以及最大重试次数；
        return new Retryer.Default(100,1,3);
    }

    @Bean
    public Logger.Level feigLoggerLevel(){
        return Logger.Level.FULL;
    }
}
