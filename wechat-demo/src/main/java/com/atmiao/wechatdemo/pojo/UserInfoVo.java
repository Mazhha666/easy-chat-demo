package com.atmiao.wechatdemo.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author miao
 * @version 1.0
 */
@Data
public class UserInfoVo implements Serializable {
    private static final long serialVersionUID = -3426636247940990341L;
    private String userId;
    private String nickName;
    private Integer joinType;
    private Integer sex;
    private String personalSignature;
    private String areaName;
    private String areaCode;
    private Integer contactStatus;
    private Boolean admin;
    private String token;

}
