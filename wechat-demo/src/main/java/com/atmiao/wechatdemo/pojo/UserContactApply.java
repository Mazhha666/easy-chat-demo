package com.atmiao.wechatdemo.pojo;

import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName user_contact_apply
 */
@TableName(value ="user_contact_apply")
@Data
public class UserContactApply implements Serializable {
    /**
     * 
     */
    @TableId(value = "apply_id", type = IdType.AUTO)
    private Integer applyId;

    /**
     * 
     */
    @TableField(value = "apply_user_id")
    private String applyUserId;

    /**
     * 
     */
    @TableField(value = "receive_user_id")
    private String receiveUserId;

    /**
     * 
     */
    @TableField(value = "contact_type")
    private Integer contactType;

    /**
     * 
     */
    @TableField(value = "contact_id")
    private String contactId;

    /**
     * 
     */
    @TableField(value = "last_apply_time")
    private Long lastApplyTime;

    /**
     * 
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 
     */
    @TableField(value = "apply_info")
    private String applyInfo;
    //是否查询
    @TableField(exist = false)
    private Boolean queryContactInfo;
    //关联查询，群组或者人
    @TableField(exist = false)
    private String contactName;
    //显示拉黑状态
    @TableField(exist = false)
    private String statusName;

    public String getStatusName() {
        UserContactStatusEnum statusEnum = UserContactStatusEnum.getByStatus(status);
        return statusName == null ? null :statusEnum.getDesc();
    }


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}