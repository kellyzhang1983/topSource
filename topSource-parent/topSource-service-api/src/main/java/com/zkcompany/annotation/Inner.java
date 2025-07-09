package com.zkcompany.annotation;

import java.lang.annotation.*;

/**
 * 申明注释：只用于内部访问
 */
@Target(ElementType.METHOD)  // 标注在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时生效
@Documented
public @interface Inner {
}
