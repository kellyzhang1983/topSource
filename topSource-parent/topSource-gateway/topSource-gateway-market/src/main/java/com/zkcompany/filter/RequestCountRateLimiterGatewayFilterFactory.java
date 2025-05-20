package com.zkcompany.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RequestCountRateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestCountRateLimiterGatewayFilterFactory.Config> {

    private final RateLimiterRegistry rateLimiterRegistry;

    public RequestCountRateLimiterGatewayFilterFactory(RateLimiterRegistry rateLimiterRegistry) {
        super(Config.class);
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public GatewayFilter apply(Config config) {
        // 获取限流器实例
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(config.getName());
        return (exchange, chain) -> {
            // 调试日志
            log.info("RateLimiter '{}' , Remaining Permits: {}", config.getName(), rateLimiter.getMetrics().getAvailablePermissions());

            // 检查是否允许请求
            if (rateLimiter.acquirePermission()) {
                return chain.filter(exchange);
            } else {
                // 触发限流逻辑
                return handleRateLimitExceeded(exchange);
            }
        };
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        // 设置响应状态码为 429 Too Many Requests
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        // 设置响应头
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        // 设置响应体
        String responseBody = "{\"flag\": false,\"code\": 429, \"message\": \"goods_gateway_Ratelimiter：你的请求被限流了，请稍后再试.....！\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));

        // 返回响应
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
