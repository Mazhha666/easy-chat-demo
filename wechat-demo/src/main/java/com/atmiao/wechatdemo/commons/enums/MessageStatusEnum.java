package com.atmiao.wechatdemo.commons.enums;

/**
 * @author miao
 * @version 1.0
 */
public enum MessageStatusEnum {
    SENDING(0,"发送中"),
    SENDED(1,"已发送");
    private Integer status;
    private String desc;

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageStatusEnum getByType(Integer type){
        for (MessageStatusEnum value :MessageStatusEnum.values()) {
            if(value.getStatus().equals(type)){
                return value;
            }
        }
        return null;
    }

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
