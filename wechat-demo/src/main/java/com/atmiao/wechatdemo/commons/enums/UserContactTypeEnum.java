package com.atmiao.wechatdemo.commons.enums;

import com.alibaba.druid.util.StringUtils;

/**
 * @author miao
 * @version 1.0
 */
public enum UserContactTypeEnum {
    USER(0,"U","好友"),
    GROUP(1,"G","群组");
    private Integer type;
    private String prefix;
    private String desc;

    UserContactTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }
    public static UserContactTypeEnum geByName(String name){
        try {
            if(StringUtils.isEmpty(name)){
                return null;
            }
            return UserContactTypeEnum.valueOf(name.toUpperCase());
        } catch (Exception e){
            return null;
        }
    }
    public static UserContactTypeEnum geByPrefix(String prefix){
        try {
            if(StringUtils.isEmpty(prefix) || prefix.trim().isEmpty()){
                return null;
            }
            prefix = prefix.substring(0,1);//获得识别 如U123456 G1234
            for (UserContactTypeEnum typeEnum : UserContactTypeEnum.values()) {
                if(typeEnum.getPrefix().equals(prefix)){
                    return typeEnum;
                }
            }
            return null;
        } catch (Exception e){
            return null;
        }
    }

    public Integer getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDesc() {
        return desc;
    }
}
