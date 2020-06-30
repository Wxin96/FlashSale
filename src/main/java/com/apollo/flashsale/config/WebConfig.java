package com.apollo.flashsale.config;

import com.apollo.flashsale.resolver.FlashSaleUserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 *  FlashSaleUserArgumentResolver需要通过@Autowired注入方式, 因为FlashSaleUserArgumentResolver类中使用了@Autowired注入其他类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    FlashSaleUserArgumentResolver resolver;
    /**
     *  注入自定义 处理器方法参数解析器
     * @param resolvers 自定义处理器方法参数解析器 集合
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }

}
