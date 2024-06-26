package com.atmiao.wechatdemo.commons.enums;

import com.alibaba.druid.util.StringUtils;

/**
 * @author miao
 * @version 1.0
 */
public enum UserContactApplyStatusEnum {
    INIT(0,"待处理"),
    PASS(1,"已同意"),
    REJECT(2,"已拒绝"),
    BLACKLIST(3,"已拉黑");
    private Integer status;
    private String desc;
    public static  UserContactApplyStatusEnum getByStatus(String status){
        try {
            if(StringUtils.isEmpty(status)){
                return null;
            }
            return  UserContactApplyStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
    public static  UserContactApplyStatusEnum getByStatus(int status){
        for ( UserContactApplyStatusEnum item :  UserContactApplyStatusEnum.values()) {
            if(item.status == status){
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

    UserContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
