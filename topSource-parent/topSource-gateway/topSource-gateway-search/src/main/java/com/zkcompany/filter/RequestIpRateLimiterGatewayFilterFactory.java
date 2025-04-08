package com.zkcompany.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RequestIpRateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestIpRateLimiterGatewayFilterFactory.Config> {

    private final RateLimiterRegistry rateLimiterRegistry;
    //private final KeyResolver ipKeyResolver;

    public RequestIpRateLimiterGatewayFilterFactory(RateLimiterRegistry rateLimiterRegistry) {
        super(Config.class);
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 获取客户端 IP 地址,并且进行分组统计（flatMap）
            return config.getKeyResolver().resolve(exchange).flatMap(ip -> {
                // 根据 IP 地址创建唯一的限流器名称
                String rateLimiterName = config.getName() + "_" + ip;

                // 获取或创建限流器实例
                RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(config.getName());

                // 调试日志
                log.info("RateLimiter '{}' triggered. IP: {}, Remaining Permits: {}", rateLimiterName, ip, rateLimiter.getMetrics().getAvailablePermissions());

                // 检查是否允许请求
                if (rateLimiter.acquirePermission()) {
                    return chain.filter(exchange);
                } else {
                    // 触发限流逻辑
                    return handleRateLimitExceeded(exchange);
                }
            });
        };
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        // 设置响应状态码为 429 Too Many Requests
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        // 设置响应头
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        // 设置响应体
        String responseBody = "{\"flag\": false,\"code\": 429, \"message\": \"search_gateway_Ratelimiter：你同一时间请求太多次，请稍后再试.....！\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));

        // 返回响应
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
        private String name;

        private KeyResolver keyResolver;

        public KeyResolver getKeyResolver() {
            return keyResolver;
        }

        public void setKeyResolver(KeyResolver keyResolver) {
            this.keyResolver = keyResolver;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
