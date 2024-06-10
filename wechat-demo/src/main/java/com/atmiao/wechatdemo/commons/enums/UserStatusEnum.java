package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum UserStatusEnum {
    DISABLE(0,"禁用"),
    ENABLE(1,"启用");
    private Integer status;
    private String desc;
    public static UserStatusEnum getByStatus(Integer status){
        for (UserStatusEnum item : UserStatusEnum.values()) {
             if(item.getStatus() == status){
                 return item;
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

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
