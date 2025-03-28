package com.zkcompany.interceptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zkcompany.entity.JwtUtil;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class AuthorizationUserFilter extends OncePerRequestFilter {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String GATWAY_TOKEN = request.getHeader("GATWAY_TOKEN");

        if(StringUtils.isEmpty(GATWAY_TOKEN)){
            //todo 不是从网关访问，返回，不予放行
            redisTemplate.boundValueOps(SystemConstants.redis_errorSecurityUserService_message).set("请从网关访问，不能直接访问服务!");
            throw new RuntimeException("请从网关访问，不能直接访问服务!");
        }else{
            //解析token后。是一个JSONObject,数据格式{user:{name:XXX,password:XXX},user_role:['ROLE_user','ROLE_admin']}这种格式
            JSONObject token_userJsonObject = JSON.parseObject(GATWAY_TOKEN);
            JSONObject token_userObject = (JSONObject) token_userJsonObject.get("user");
            List<String> token_roleList = (List<String>) token_userJsonObject.get("user_role");
            //判断缓存里面是否还存有用户的token，如果没有，说明用户已退出认证失败。

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
