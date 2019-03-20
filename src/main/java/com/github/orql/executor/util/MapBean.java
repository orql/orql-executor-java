package com.github.orql.executor.util;

import java.lang.reflect.Field;
import java.util.*;

public class MapBean {

    private static final Class<?>[] BaseTypes = {
            String.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Date.class
    };

    /**
     * map to class
     * @param map
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toBean(Map<String, Object> map, Class<T> clazz) {
        try {
            T obj = clazz.newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Field field = clazz.getDeclaredField(key);
                if (field == null) continue;
                Object value = entry.getValue();
                if (value == null) continue;
                field.setAccessible(true);
                if (value instanceof List) {
                    Class genericClazz = ReflectUtil.getGenericClazz(field);
                    List list = new ArrayList();
                    for (Object childValue : (List) value) {
                        list.add(toBean((Map) childValue, genericClazz));
                    }
                    field.set(obj, list);
                } else if (value instanceof Map) {
                    Class childClazz = field.getType();
                    field.set(obj, toBean((Map) value, childClazz));
                } else if (field.getType().isEnum()) {
                    // enum
                    field.set(obj, Enum.valueOf((Class<Enum>) field.getType(), (String) value));
                } else if (field.getType() == Boolean.class) {
                    // boolean int
                    if (value instanceof Integer) {
                        field.set(obj, (int) value == 1);
                    } else {
                        // boolean
                        field.set(obj, value);
                    }
                } else {
                    // value
                    field.set(obj, value);
                }
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class BeanToMap {
        Map<Object, Map<String, Object>> objects = new HashMap<>();

        Map<String, Object> toMap(Object obj) {
            Map<String, Object> map = new HashMap<>();
            // 嵌套情况
            if (objects.containsKey(obj)) return objects.get(obj);
            objects.put(obj, map);
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value == null) continue;
                    Class type = field.getType();
                    if (value instanceof List) {
                        // list
                        List list = new ArrayList();
                        for (Object childValue : (List) value) {
                            list.add(toMap(childValue));
                        }
                        map.put(field.getName(), list);
                    } else if (type.isPrimitive() || isBaseType(type)) {
                        // primitive
                        map.put(field.getName(), value);
                    } else if (type.isEnum()) {
                        // enum
                        map.put(field.getName(), ((Enum) value).name());
                    } else {
                        // class
                        map.put(field.getName(), toMap(value));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return map;
        }
    }

    /**
     * bean to map
     * @param obj
     * @return
     */
    public static Map<String, Object> toMap(Object obj) {
        BeanToMap beanToMap = new BeanToMap();
        return beanToMap.toMap(obj);
    }

    private static boolean isBaseType(Class clazz) {
        for (Class baseType : BaseTypes) {
            if (baseType == clazz) return true;
        }
        return false;
    }
}
