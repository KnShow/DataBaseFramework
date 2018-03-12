package com.kn0527.cn.databaseframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * auto：xkn on 2018/3/12 13:28
  @Target说明了Annotation所修饰的对象范围:
  Annotation可被用于 packages、types（类、接口、枚举、Annotation类型）、
  类型成员（方法、构造方法、成员变量、枚举值）、方法参数和本地变量（如循环变量、catch参数）
  @Retebtuib:注解的保留位置
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbTable{
    String value();
}
