package com.hangzhou.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Api方法鉴权认证注解
 * @Author linchenghui
 * @Date 2021/4/13
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface ApiAuthorization {
}
