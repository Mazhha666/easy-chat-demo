package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum GroupStatusEnum {
    NOMAL(1,"正常"),
    DISSOLUTION(0,"解散");
    private Integer status;
    private String desc;
    public static GroupStatusEnum getByStatus(Integer status){
        for (GroupStatusEnum item : GroupStatusEnum.values()) {
            if(item.status == status){
                return item;
            }
        }
        return null;
    }
    GroupStatusEnum(Integer status, String desc) {
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
