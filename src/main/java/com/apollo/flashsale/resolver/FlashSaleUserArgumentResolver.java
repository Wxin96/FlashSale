package com.apollo.flashsale.resolver;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.service.FlashSaleUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  参数绑定, 将Controller层中方法中的FlashSaleUser类型参数绑定
 *  要想通过@Autowired注入FlashSaleService类, 必须将FlashSaleUserArgumentResolver添加到Spring容器中
 */
@Component
public class FlashSaleUserArgumentResolver implements HandlerMethodArgumentResolver {

    Logger logger = LoggerFactory.getLogger(FlashSaleUserArgumentResolver.class);

    @Autowired
    FlashSaleUserService flashSaleUserService;

    /**
     *  判断方法中的参数, 是否是 FlashSaleUser
     * @param parameter 要检查的方法参数
     * @return 如果此解析器支持所提供的参数, 返回true；否则, 返回false;
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> parameterType = parameter.getParameterType();
        return parameterType == FlashSaleUser.class;
    }

    /**
     *  将方法参数解析为给定请求的参数值。
     * @param parameter 要解析的方法参数。前提: supportsParameter()方法返回true
     * @param mavContainer 当前请求的ModelAndViewContainer
     * @param webRequest 当前请求
     * @param binderFactory 用于创建 WebDataBinder 的实例工厂
     * @return 解析的参数值；如果无法解析，则为 null
     * @throws Exception  如果在准备参数值时出错, 抛出异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // 1.获取request和response
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        // 2.从参数或者Cookie中获取 token(uuid)
        String paramToken = request.getParameter(FlashSaleUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, FlashSaleUserService.COOKIE_NAME_TOKEN);
        logger.info("请求参数中paramToken:" + paramToken);
        logger.info("Cookie中cookieToken:" + cookieToken);

        // 3.根据这两个参数, 确定优先级获取FlashSaleUser
        if (StringUtils.isEmpty(paramToken) && StringUtils.isEmpty(cookieToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        logger.debug("Cookie标识token:" + token);

        return flashSaleUserService.getByToken(response, token);
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
