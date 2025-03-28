package com.zkcompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@SpringBootApplication
@EnableDiscoveryClient
public class OrderGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderGatewayApplication.class,args);
    }

    //创建一个ipKeyResolver 指定用户的IP
    @Bean(name="ipKeyResolver")
    public KeyResolver keyResolver(){
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                return Mono.just(exchange.getRequest().getRemoteAddress().getHostString());
            }
        };
    }
}
