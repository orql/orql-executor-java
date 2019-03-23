package com.github.orql.executor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsToMany {

    /**
     * many to many middle table
     * @return
     */
    Class<?> middle();

    /**
     * current schema field in middle
     * @return
     */
    String middleKey() default "";

    /**
     * ref schema field in middle
     * @return
     */
    String refMiddleKey() default "";
}
