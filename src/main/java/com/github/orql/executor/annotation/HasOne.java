package com.github.orql.executor.annotation;

import com.github.orql.executor.Cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HasOne {
    String refKey() default "";
    boolean required() default true;
    Cascade onDelete() default Cascade.Restrict;
    Cascade onUpdate() default Cascade.Restrict;
}
