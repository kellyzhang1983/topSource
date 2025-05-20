package com.zkcompany.interceptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class AuthorizationGoodsFilter extends OncePerRequestFilter {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${security.ignored}")
    private String[] ignoredUrls;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1、获取请求路径、表头信息，白名单路径。
        String contextPath = request.getRequestURI();
        String gatewayHeader = request.getHeader("reuqest-from-gateway");
        String feginIntereptor = request.getHeader("fegin-intereptor");
        String timerTaskOrListenerMQ = request.getHeader("timerTaskOrListenerMQ");
        String GATWAY_TOKEN = request.getHeader("GATWAY_TOKEN");
        boolean contains = Arrays.asList(ignoredUrls).contains(contextPath);
        //2、首先判断是不是从fegin接口的拦截器过来的请求，如果是拦截器过来的请求并且请求地址在白名单内，进行放行，如果请求地址不在白名单内，security后续过滤器会过滤掉
        if(!StringUtils.isEmpty(feginIntereptor)){
            filterChain.doFilter(request,response);
            return;
        }

        //3. 判断请求是否来自内部的定时任务
        if(!StringUtils.isEmpty(timerTaskOrListenerMQ)){
            //调用UsernamePasswordAuthenticationToken标明这个对象，已通过验证，后续来拦截器不会拦截。
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(null, null, null);
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            filterChain.doFilter(request,response);
            return;
        }

        //4、判断是不是从网关进行访问
        if(StringUtils.isEmpty(gatewayHeader)){
            if(StringUtils.isEmpty(GATWAY_TOKEN)){
                redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityGoodsService_message).set("请从网关进行访问!");
                throw new RuntimeException("请从网关进行访问!");
            }
        }
        //5、其次再判断请求地址是否加入白名单，如果在白名内，自动放行
        if(contains){
            filterChain.doFilter(request,response);
            return;
        }

        if(StringUtils.isEmpty(GATWAY_TOKEN)){
            //todo 不是从网关访问，返回，不予放行
            redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityGoodsService_message).set("请携带token进行访问!");
            throw new RuntimeException("请携带token进行访问!");
        }else{
            //解析token后。是一个JSONObject,数据格式{user:{name:XXX,password:XXX},user_role:['ROLE_user','ROLE_admin']}这种格式
            JSONObject token_userJsonObject = JSON.parseObject(GATWAY_TOKEN);
            JSONObject token_userObject = (JSONObject) token_userJsonObject.get("user");
            List<String> token_roleList = (List<String>) token_userJsonObject.get("user_role");
            //从表头里得到权限信息，转换成GrantedAuthority数据类型。
            Collection<GrantedAuthority> userRole = new ArrayList<GrantedAuthority>();
            if (!(token_roleList == null || token_roleList.size() == 0)) {
                for (String roleString : token_roleList) {
                    SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(roleString);
                    userRole.add(simpleGrantedAuthority);
                }
            }

            User user = (User)redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(token_userObject.get("id"));

            //调用UsernamePasswordAuthenticationToken标明这个对象，已通过验证。
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, null, userRole);
            //把user这个对象存入上下文路径当中。
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        }
        filterChain.doFilter(request,response);
    }
}
