package com.zkcompany.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zkcompany.entity.JwtUtil;
import com.zkcompany.entity.Result;
import com.zkcompany.entity.SystemConstants;
import com.zkcompany.pojo.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class AuthorizationSecurityUserFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从request的请求当中获取token。
        String authorization = request.getHeader("Authorization");
        //获户端的url地址。
        String contextPath = request.getRequestURI();

        //log.info("contextPath:" + contextPath);
        if(StringUtils.isEmpty(authorization)){
            filterChain.doFilter(request,response);
            //如果客户端的地址是 Login,那么过滤器放行
            if(contextPath.equals("/userAuthentication/login")){
                filterChain.doFilter(request,response);
            }else{
                //如果地址不是login，那么返回给客户端错误信息。
                redisTemplate.boundValueOps(SystemConstants.redis_errorSecurity_message).set("请先登录！把需要的token信息传入过来！");
                throw new RuntimeException("请先登录！把需要的token信息传入过来！") ;
            }
            return;
        }


        //从jwtsUtil来识别token
        String sub = "";
        try {
            //利用jwt工具类，解析token。
            Claims claims = JwtUtil.parseJWT(authorization);
            sub = claims.getSubject();
        } catch (Exception e) {;
            //redisTemplate.boundHashOps(SystemConstants.redis_userToken).delete("");
            redisTemplate.boundValueOps(SystemConstants.redis_errorSecurity_message).set("解析token失败!请查验token的有效性，没有权限进行访问(Message):" + e.getMessage());
            throw new RuntimeException("解析token失败!请查验token的有效性，没有权限进行访问(Message):" + e.getMessage()) ;
        }
        //解析token后。是一个JSONObject,数据格式{user:{name:XXX,password:XXX},user_role:['ROLE_user','ROLE_admin']}这种格式
        JSONObject token_userJsonObject = JSON.parseObject(sub);
        JSONObject token_userObject = (JSONObject) token_userJsonObject.get("user");
        List<String> token_roleList = (List<String>)token_userJsonObject.get("user_role");
        //判断缓存里面是否还存有用户的token，如果没有，说明用户已退出认证失败。
        Object user_token = redisTemplate.boundHashOps(SystemConstants.redis_userToken).get(token_userObject.get("username"));

        if(Objects.isNull(user_token)){
            redisTemplate.boundValueOps(SystemConstants.redis_errorSecurity_message).set("用户已退出登录！请重新登录!");
            throw new RuntimeException("用户已退出登录！请重新登录!");
        }

        Collection<GrantedAuthority> userRole = new ArrayList<GrantedAuthority>();
        if(!(token_roleList == null || token_roleList.size() == 0)){
            for(String roleString : token_roleList){
                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(roleString);
                userRole.add(simpleGrantedAuthority);
            }
        }
        User user = (User)redisTemplate.boundHashOps(SystemConstants.redis_userInfo).get(token_userObject.get("id"));

        //调用UsernamePasswordAuthenticationToken标明这个对象，已通过验证。
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user,null,userRole);
        //把user这个对象存入上下文路径当中。
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        filterChain.doFilter(request,response);

    }
}
