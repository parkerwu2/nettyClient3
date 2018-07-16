package com.example.demo.util;

import java.util.regex.Pattern;

/**
 * Created by jingzhi.wu on 2018/7/16.
 */
public class RegexUtil {
    public static boolean isRightMethod(String line){
        String regex = "^[0-7]$";
        return Pattern.matches(regex, line);
    }

//    public static void main(String[] args) {
//        System.out.println(isRightMethod("8"));
//        System.out.println(isRightMethod("a"));
//        System.out.println(isRightMethod("cd"));
//        System.out.println(isRightMethod("1.2"));
//        System.out.println(isRightMethod("0"));
//    }
}
