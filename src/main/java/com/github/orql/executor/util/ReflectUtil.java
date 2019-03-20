package com.github.orql.executor.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectUtil {

    /**
     * 获取List<T> T的类型
     * @param field
     * @return
     */
    public static Class<?> getGenericClazz(Field field) {
        Type genericType = field.getGenericType();
        ParameterizedType pt = (ParameterizedType) genericType;
        // T class type
        return (Class<?>)pt.getActualTypeArguments()[0];
    }

}
