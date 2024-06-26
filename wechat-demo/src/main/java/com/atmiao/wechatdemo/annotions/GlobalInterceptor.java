package com.atmiao.wechatdemo.annotions;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author miao
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})

public @interface GlobalInterceptor {
    boolean checkLogin() default true;
    boolean checkAdmin() default false;
}
