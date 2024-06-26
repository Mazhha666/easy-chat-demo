package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum JoinTypeEnum {
    JOIN(0,"直接加入"),
    APPLY(1,"需要审核");
    private Integer status;
    private String desc;

    JoinTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
