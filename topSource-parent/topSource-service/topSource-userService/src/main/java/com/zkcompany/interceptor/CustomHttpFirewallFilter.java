package com.zkcompany.interceptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class CustomHttpFirewallFilter extends OncePerRequestFilter {

    private final HttpFirewall httpFirewall;

    public CustomHttpFirewallFilter(HttpFirewall httpFirewall) {
        this.httpFirewall = httpFirewall;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            HttpServletRequest firewalledRequest = httpFirewall.getFirewalledRequest(request);
            filterChain.doFilter(firewalledRequest,response);
        } catch (RequestRejectedException e) {
            throw new RuntimeException(e);
        }
    }
}
