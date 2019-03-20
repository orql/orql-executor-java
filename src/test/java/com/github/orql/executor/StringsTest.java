package com.github.orql.executor;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringsTest {

    @Test
    public void testA() {
        String typePatternString = "(.+?)\\((.+?)\\)";
        Pattern typePattern = Pattern.compile(typePatternString);
        Matcher m = typePattern.matcher("bigint(10)");
        if (m.find()) {
            System.out.println(m.group(1));
            System.out.println(m.group(2));
        }
    }

}
