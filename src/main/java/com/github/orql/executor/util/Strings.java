package com.github.orql.executor.util;

import java.util.List;

public class Strings {

    public static String join(List list, String separator) {
        return join(list.toArray(), separator);
    }

    public static String join(Object[] list, String separator) {
        if (list.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.length; i ++) {
            builder.append(list[i]).append(separator);
        }
        builder.setLength(builder.length() - separator.length());
        return builder.toString();
    }

    public static String toLowerCaseFirst(String string) {
        if (Character.isLowerCase(string.charAt(0))) {
            return string;
        }
        return (new StringBuilder()).append(Character.toLowerCase(string.charAt(0))).append(string.substring(1)).toString();
    }

    /**
     * 判断字符串出现次数
     * @param str
     * @param match
     * @return
     */
    public static int countMatches(String str, String match) {
        return str.length() - str.replace(match, "").length();
    }

}
