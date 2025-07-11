package com.zkcompany.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.entity.JwtUtil;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.SystemConstants;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Component
public class AuthorizeGoodsGatewayFilter implements GlobalFilter, Ordered {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${security.ignored}")
    private String[] ignoredUrls;

    private String sub = "";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //2.获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        //3.判断请求路径是否在白名单内，如果在白名单内，请求放行！
        String requestURI = request.getURI().getPath();
        boolean contains = Arrays.asList(ignoredUrls).contains(requestURI);
        if(contains){
            //.header("reuqest-from-gateway","true")
            Consumer<HttpHeaders> httpHeaders = new Consumer<HttpHeaders>() {
                @Override
                public void accept(HttpHeaders httpHeaders) {
                    httpHeaders.add("fegin-intereptor-whitelist", "true");
                    httpHeaders.add("reuqest-from-gateway","true");
                }
            };
            ServerHttpRequest httpRequest = request.mutate().headers(httpHeaders).build();
            return chain.filter(exchange.mutate().request(httpRequest).build());
        }

        ///3 从头header中获取令牌数据
        String authorization = request.getHeaders().getFirst("Authorization");

        if(StringUtils.isEmpty(authorization)){
            //throw new RuntimeException("没有token，表头Authorization为空");
            //判断表头header是否有表头Authorization这个字段，如果没有进行拦截，不予通过
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
            Result<Object> result = new Result<>(false, response.getStatusCode().value(), "请求头中没有Authorization！验证不通过!请先登录......");
            String result_jsonString = JSON.toJSONString(result);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(result_jsonString.getBytes())));
        }

        //4 利用jwtsUtil工具来识别token
        try {
            //利用jwt工具类，解析token。
            Claims claims = JwtUtil.parseJWT(authorization);
            sub = claims.getSubject();
        } catch (Exception e) {
            //throw new RuntimeException("解析token失败!请查验token的有效性，没有权限进行访问(Message):" + e.getMessage()) ;
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
            Result<Object> result = new Result<>(false, response.getStatusCode().value(), "解析token失败!请查验token的有效性，没有权限进行访问(Message):" + e.getMessage());
            String result_jsonString = JSON.toJSONString(result);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(result_jsonString.getBytes())));
        }
        //5、解析完后把字符串转成JSON格式
        JSONObject token_userJsonObject = JSON.parseObject(sub);
        //如果是用oauth2server登录，那么需要验证scopes，如果是内部登录，则不需要验证
        List<String> scopes = token_userJsonObject.get("scopes") == null ? new ArrayList<>() : (List<String>) token_userJsonObject.get("scopes");
        if(scopes.size() != 0){
            if(!scopes.contains("app")){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
                Result<Object> result = new Result<>(false, response.getStatusCode().value(), "没有权限访问该服务，请联系管理员！");
                String result_jsonString = JSON.toJSONString(result);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(result_jsonString.getBytes())));
            }
        }


        JSONObject token_userObject = (JSONObject) token_userJsonObject.get("user");
        Object user_token = redisTemplate.boundHashOps(SystemConstants.redis_userToken).get(token_userObject.get("username"));
        //6、判断用户token是否被注销，如果注销，请重新登录
        if(Objects.isNull(user_token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
            Result<Object> result = new Result<>(false, response.getStatusCode().value(), "用户未登录，请登录后再访问:");
            String result_jsonString = JSON.toJSONString(result);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(result_jsonString.getBytes())));
        }

        //7、添加头信息 传递给微服务
        //ServerHttpRequest json_token = request.mutate().header("GATWAY_TOKEN", sub).build();
        Consumer<HttpHeaders> gatway_token = new Consumer<HttpHeaders>() {
            @Override
            public void accept(HttpHeaders httpHeaders) {
                httpHeaders.add("GATWAY_TOKEN", sub);
                httpHeaders.add("reuqest-from-gateway","true");
            }
        };
        ServerHttpRequest json_token = request.mutate().headers(gatway_token).build();
        return chain.filter(exchange.mutate().request(json_token).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
