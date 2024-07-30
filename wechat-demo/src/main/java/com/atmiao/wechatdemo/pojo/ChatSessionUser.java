package com.atmiao.wechatdemo.pojo;

import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @TableName chat_session_user
 */
@TableName(value ="chat_session_user")
@Data
public class ChatSessionUser implements Serializable {
    /**
     * 
     */
    @TableId(value = "user_id")
    private String userId;

    /**
     * 联合主键，相应service的save and update 慎用
     */
    @TableField(value = "contact_id")
    private String contactId;

    /**
     * 
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 
     */
    @TableField(value = "contact_name")
    private String contactName;
    @TableField(exist = false)
    private String lastMessage;
    @TableField(exist = false)
    private Long lastReceiveTime;
    @TableField(exist = false)
    private Integer memberCount;
    @TableField(exist = false)
    private Integer contactType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public Integer getContactType() {
        if(StringUtils.isEmpty(contactId)){
            return null;
        }
        return UserContactTypeEnum.geByPrefix(contactId).getType();
    }
}