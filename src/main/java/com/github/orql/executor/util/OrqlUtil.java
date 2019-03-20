package com.github.orql.executor.util;

public class OrqlUtil {

    public static String getKeyword(String orql) {
        String[] arr = orql.split(" ", 2);
        return arr[0];
    }

    public static String getSchema(String orql) {
        String[] arr = orql.split(" |:", 3);
        return arr.length == 3 ? arr[1] : null;
    }

}
