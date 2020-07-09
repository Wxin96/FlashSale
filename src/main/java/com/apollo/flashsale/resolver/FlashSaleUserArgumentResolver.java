package com.apollo.flashsale.resolver;

import com.apollo.flashsale.access.UserContext;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.service.FlashSaleUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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
        logger.trace("解析FlashSaleUser用户~");
        // 1.从线程中获取
        FlashSaleUser user = UserContext.getUser();
        // 2.判断是否为空
        if (user == null) {
            logger.warn("解析用户为空!");
        }
        return user;
    }


}
