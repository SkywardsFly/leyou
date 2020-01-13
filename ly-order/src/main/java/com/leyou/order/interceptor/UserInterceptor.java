package com.leyou.order.interceptor;


import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());

        //解析token
        try {
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
            //传递user
            tl.set(userInfo);
            return true;
        } catch (Exception e) {
            log.error("【购物车服务】解析token失败",e);
            return false;

        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }
    public static UserInfo getUser(){
        return tl.get();
    }
}
