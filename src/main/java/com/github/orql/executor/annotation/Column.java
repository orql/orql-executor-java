package com.github.orql.executor.annotation;

import com.github.orql.executor.schema.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * 主键
     * @return
     */
    boolean primaryKey() default false;

    /**
     * 自动生成主键
     * @return
     */
    boolean generatedKey() default false;

    /**
     * 长度
     * @return
     */
    int length() default 0;

    /**
     * 数据库类型
     * @return
     */
    DataType dataType() default DataType.Never;

    /**
     * 数据库字段
     * @return
     */
    String field() default "";

    /**
     * 是否必须
     * @return
     */
    boolean required() default false;
}
