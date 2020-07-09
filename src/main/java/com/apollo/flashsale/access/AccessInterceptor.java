package com.apollo.flashsale.access;

import com.alibaba.fastjson.JSON;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.redis.key.impl.AccessKey;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.FlashSaleUserService;
import com.apollo.flashsale.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    FlashSaleUserService userService;

    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断是不是方法上的注解
        if (handler instanceof HandlerMethod) {
            // 1.根据请求获取用户, 并保存在ThreadLocal中
            FlashSaleUser user =  gerUser(request, response);
            UserContext.setUser(user);
            // 2.使用handler获取注解的值
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            // 3.判断注解是否为空, 为空放行
            if (accessLimit == null) {
                return true;
            }
            // 4.获取注解的值
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            // 5.根据是否需要登录执行不同的命令
            String userLoginLimit = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                } else {
                    userLoginLimit += "_" + user.getId();
                }
            } else {
                // do nothing
                return true;
            }
           // 6.利用Redis缓存设置登录限制
            AccessKey accessKey = AccessKey.withExpire(seconds);
            Integer count = redisService.get(accessKey, userLoginLimit, Integer.class);
            if(count  == null) {
                log.trace("用户" + user.getId() + "第一次登陆~");
                redisService.set(accessKey, userLoginLimit, 1);
            }else if(count < maxCount) {
                Long incr = redisService.incr(accessKey, userLoginLimit);
                log.trace("用户" + user.getId() + "第" + incr + "次登陆");
            }else {
                log.warn("用户" + user.getId() + "登录次数频繁, 超出限制");
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }

        return true;
    }

    /**
     * 利用response响应发送数据
     * @param response 响应
     * @param codeMsg 通过响应response发送的消息
     * @throws Exception
     */
    private void render(HttpServletResponse response, CodeMsg codeMsg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = response.getOutputStream();
        String responseBody = JSON.toJSONString(Result.error(codeMsg));
        outputStream.write(responseBody.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }

    /**
     *  根据请求request和响应response获取秒杀用户信息
     * @param request 请求
     * @param response 响应
     * @return 秒杀用户
     */
    private FlashSaleUser gerUser(HttpServletRequest request, HttpServletResponse response) {
        // 1.从参数或者Cookie中获取 token(uuid)
        String paramToken = request.getParameter(FlashSaleUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, FlashSaleUserService.COOKIE_NAME_TOKEN);
        log.debug("请求参数中paramToken:" + paramToken);
        log.debug("Cookie中cookieToken:" + cookieToken);

        // 2.根据这两个参数, 确定优先级获取FlashSaleUser
        if (StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(cookieToken)) {
            log.debug("Cookie和请求参数中均没有token的值!");
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        log.debug("Cookie标识token:" + token);

        return userService.getByToken(response, token);
    }

    /**
     *  寻找以 cookieName 为键的Cookie
     * @param request 请求
     * @param cookieName Cookie键
     * @return 若对应的减存在, 返回对应的cookie值; 不存在返回null
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        // 1.获取Cookies
        Cookie[] cookies = request.getCookies();
        // 2.处理空指针异常
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        // 3.寻找key==cookieName的Cookie
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
