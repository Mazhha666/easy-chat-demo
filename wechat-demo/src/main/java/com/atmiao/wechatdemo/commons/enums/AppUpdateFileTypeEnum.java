package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum AppUpdateFileTypeEnum {
    LOCAL(0,"本地"),
    OUTER_LINK(1,"外链");
    private Integer type;
    private String desc;
    public static AppUpdateFileTypeEnum getByType(Integer type){
        for (AppUpdateFileTypeEnum value : AppUpdateFileTypeEnum.values()) {
            if(value.getType().equals(type)){
                return value;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    AppUpdateFileTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
