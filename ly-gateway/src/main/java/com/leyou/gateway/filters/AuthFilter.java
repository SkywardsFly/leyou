package com.leyou.gateway.filters;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private FilterProperties filterProp;
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE; //過濾類型
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1; //過濾器順序
    }

    //是否过滤
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String path = ctx.getRequest().getRequestURI();
        //放行则返回false
        return !isAllowPath(path);
    }

    private Boolean isAllowPath(String path) {
        for (String allowPath : filterProp.getAllowPaths()){
            if (path.startsWith(allowPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() {
        //獲取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //獲取request
        HttpServletRequest request = ctx.getRequest();
        //獲取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        //解析token
        try {
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
        } catch (Exception e) {
            //解析未登錄拦截
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(404);
            log.error("非法访问，未登录，地址：{}", request.getRemoteHost(), e);
        }
        return null;
    }
}
