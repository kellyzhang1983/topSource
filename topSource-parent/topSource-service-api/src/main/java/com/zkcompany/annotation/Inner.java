package com.zkcompany.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)  // 标注在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时生效
@Documented
public @interface Inner {
}
