package com.atmiao.wechatdemo.pojo;

import com.atmiao.wechatdemo.commons.Constants;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.Data;

/**
 * 
 * @TableName user_info
 */
@TableName(value ="user_info")
@Data
public class UserInfo implements Serializable {
    /**
     * 
     */
    @TableId(value = "user_id")
    private String userId;

    /**
     * 
     */
    @TableField(value = "email")
    private String email;

    /**
     * 
     */
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 
     */
    @TableField(value = "join_type")
    private Integer joinType;

    /**
     * 
     */
    @TableField(value = "sex")
    private Integer sex;

    /**
     * 
     */
    @TableField(value = "password")
    private String password;

    /**
     * 
     */
    @TableField(value = "personal_signature")
    private String personalSignature;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 
     */
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 
     */
    @TableField(value = "area_name")
    private String areaName;

    /**
     * 
     */
    @TableField(value = "area_code")
    private String areaCode;

    /**
     * 
     */
    @TableField(value = "last_off_time")
    private Long lastOffTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    @TableField(exist = false)
    private Integer onlineType;

    public Integer getOnlineType() {
        if(lastLoginTime !=null &&  lastLoginTime.toInstant(ZoneOffset.of("+8")).toEpochMilli() > lastOffTime){
             return Constants.ONE;
        }
        return Constants.ZERO;
    }
}