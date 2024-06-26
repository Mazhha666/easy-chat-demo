package com.atmiao.wechatdemo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName group_info
 */
@TableName(value ="group_info")
@Data
public class GroupInfo implements Serializable {
    /**
     * 
     */
    @TableId(value = "group_id")
    private String groupId;

    /**
     * 
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 
     */
    @TableField(value = "group_owner_id")
    private String groupOwnerId;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 
     */
    @TableField(value = "group_notice")
    private String groupNotice;

    /**
     * 
     */
    @TableField(value = "join_type")
    private Integer joinType;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    @TableField(exist = false)
    private Integer memberCount;
    @TableField(exist = false)
    private String groupOwnerNickName;
    @TableField(exist = false)
    private Boolean queryMemberCount;
    @TableField(exist = false)
    private Boolean queryGroupOwnerNickName;



}