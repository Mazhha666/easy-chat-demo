package com.atmiao.wechatdemo.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName user_contact
 */
@TableName(value ="user_contact")
@Data
public class UserContact implements Serializable {
    /**
     * 
     */

    @TableId(value = "user_id")
    private String userId;

    /**
     * 作为联合主键,不过无所谓，
     */
//    @TableField(value = "contact_id",insertStrategy = FieldStrategy.IGNORED)
    @TableField(value = "contact_id")
    private String contactId;

    /**
     * 
     */
    @TableField(value = "contact_type")
    private Integer contactType;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "last_update_time")
    private LocalDateTime lastUpdateTime;
    //这个信息为真，才会查询
    @TableField(exist = false)
    private Boolean queryUserInfo;
    @TableField(exist = false)
    private Boolean queryGroupInfo;
    @TableField(exist = false)
    private Boolean contactUserInfo;
    @TableField(exist = false)
    private Boolean excludeMyGroup;
    @TableField(exist = false)
    private Integer[] statusArray;
    //用于关联查询userInfo
    @TableField(exist = false)
    private String contactName;
    @TableField(exist = false)
    private Integer sex;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}