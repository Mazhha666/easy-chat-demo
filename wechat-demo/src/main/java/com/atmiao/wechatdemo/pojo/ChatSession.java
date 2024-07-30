package com.atmiao.wechatdemo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName chat_session
 */
@TableName(value ="chat_session")
@Data
public class ChatSession implements Serializable {
    /**
     * 会话id
     */
    @TableId(value = "session_id")
    private String sessionId;

    /**
     * 最后接收的消息
     */
    @TableField(value = "last_message")
    private String lastMessage;

    /**
     * 最后接收消息事件毫秒
     */
    @TableField(value = "last_receive_time")
    private Long lastReceiveTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}