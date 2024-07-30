package com.atmiao.wechatdemo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 
 * @TableName chat_message
 */
@TableName(value ="chat_message")
@Data
public class ChatMessage implements Serializable {
    /**
     * 
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 
     */
    @TableField(value = "message_type")
    private Integer messageType;

    /**
     * 
     */
    @TableField(value = "message_content")
    private String messageContent;

    /**
     * 
     */
    @TableField(value = "send_user_id")
    private String sendUserId;

    /**
     * 
     */
    @TableField(value = "send_user_nick_name")
    private String sendUserNickName;

    /**
     * 
     */
    @TableField(value = "send_time")
    private Long sendTime;

    /**
     * 
     */
    @TableField(value = "contact_id")
    private String contactId;

    /**
     * 联系人类型 0 单聊 1群聊
     */
    @TableField(value = "contact_type")
    private Integer contactType;

    /**
     * 
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 
     */
    @TableField(value = "file_type")
    private Integer fileType;

    /**
     * 状态0 正在发送 1 已发送
     */
    @TableField(value = "status")
    private Integer status;
    @TableField(exist = false)
    private List<String> contactIdList;
    @TableField(exist = false)
    private Long lastReceiveTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}