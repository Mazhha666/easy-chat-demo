package com.atmiao.wechatdemo.commons.enums;

import com.alibaba.druid.util.StringUtils;

/**
 * @author miao
 * @version 1.0
 */
public enum UserContactStatusEnum {
    NOT_FRIEND(0,"非好友"),
    FRIEND(1,"好友"),
    DEL(2,"已删除好友"),
    DEL_BE(3,"被好友删除"),
    BLACKLIST(4,"已拉黑好友"),
    BLACKLIST_BE(5,"被好友拉黑"),
    BLACKLIST_BE_FIRST(6,"被好友首次拉黑");
    private Integer status;
    private String desc;
    public static UserContactStatusEnum getByStatus(String status){
        try {
            if(StringUtils.isEmpty(status)){
                return null;
            }
            return UserContactStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
    public static UserContactStatusEnum getByStatus(Integer status){
        for (UserContactStatusEnum item : UserContactStatusEnum.values()) {
            if(item.status == status){
                return item;
            }
        }
        return null;
    }

    UserContactStatusEnum(Integer status, String desc) {
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

