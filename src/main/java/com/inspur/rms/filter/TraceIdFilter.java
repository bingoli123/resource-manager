package com.inspur.rms.filter;

import cn.hutool.core.lang.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @author : lidongbin
 * @date : 2021/8/25 10:21 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Order(1)
@WebFilter(urlPatterns = "/*")
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID = "traceId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String parameter = servletRequest.getParameter(TRACE_ID);
        if (StringUtils.isNotBlank(parameter)) {
            MDC.put(TRACE_ID, parameter);
        } else {
            MDC.put(TRACE_ID, UUID.randomUUID().toString());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
