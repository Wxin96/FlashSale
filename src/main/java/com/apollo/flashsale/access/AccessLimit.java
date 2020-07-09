package com.apollo.flashsale.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {

    /**
     *  定义访问时长, 单位:s
     * @return 访问时长
     */
    int seconds();

    /**
     *  定义访问时长中, 最大访问次数
     * @return 访问时长中的最大访问次数
     */
    int maxCount();

    /**
     *  是否需要登录, 默认值为需要(true)
     * @return true(默认), 需要; false, 不需要.
     */
    boolean needLogin() default true;
}
