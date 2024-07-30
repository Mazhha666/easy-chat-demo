package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum DateTimePatternEnum {
    YYYY_MM_DD_HH_MM_MM("yyyy-MM-dd HH:mm:ss"),
    YYYYMM("yyyyMM"),
    YYYY_MM_DD("yyyy-MM-dd");
    private String pattern;

    DateTimePatternEnum(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
