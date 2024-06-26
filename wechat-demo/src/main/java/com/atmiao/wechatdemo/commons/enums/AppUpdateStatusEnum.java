package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum AppUpdateStatusEnum {
    INIT(0,"未发布"),
    GRAYSCALE(1,"灰度发布"),
    ALL(2,"全网发布");
    private Integer status;
    private String desc;
    public static AppUpdateStatusEnum getByType(Integer type){
        for (AppUpdateStatusEnum value : AppUpdateStatusEnum.values()) {
            if(value.getStatus().equals(type)){
                return value;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    AppUpdateStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
