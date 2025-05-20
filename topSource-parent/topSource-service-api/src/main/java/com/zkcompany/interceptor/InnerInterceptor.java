package com.zkcompany.interceptor;


import com.zkcompany.annotation.Inner;
import com.zkcompany.config.InnerCallSignManager;
import com.zkcompany.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class InnerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 仅拦截方法级别的请求（跳过资源请求等）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 检查方法或类是否带有 @Inner 注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        boolean isInnerMethod = handlerMethod.getMethod().isAnnotationPresent(Inner.class);
        boolean isInnerClass = handlerMethod.getBeanType().isAnnotationPresent(Inner.class);

        // 如果没有 @Inner 注解，直接放行
        if (!isInnerMethod && !isInnerClass) {
            return true;
        }
        //得到动态签名
        InnerCallSignManager innerCallSignManager = new InnerCallSignManager();
        String sign = innerCallSignManager.generateSign(request.getRequestURI());
        String headed = request.getHeader("InnerMethod-call");

        // 有 @Inner 注解时，验证请求头
        if (!sign.equals(headed)) {
            response.setStatus(403); // 403 Forbidden
            response.setContentType("application/json;charset=UTF-8");
            Result<Object> result = new Result<>(false,response.getStatus(), "此方法只允许内部访问、外部无法访问！");
            response.getWriter().println(result.toJsonString(result));
            return false;
        }
        return true;
    }
}
