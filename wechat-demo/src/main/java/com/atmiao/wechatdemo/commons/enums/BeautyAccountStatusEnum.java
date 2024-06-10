package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum BeautyAccountStatusEnum {
    NO_USE(0,"未使用"),
    USED(1,"已使用");
    private Integer status;
    private String desc;
    public static BeautyAccountStatusEnum getByStatus(Integer status){
        for (BeautyAccountStatusEnum value : BeautyAccountStatusEnum.values()) {
            if(value.getStatus() == status){
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

    BeautyAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
