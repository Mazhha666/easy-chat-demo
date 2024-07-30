package com.atmiao.wechatdemo.dto;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;

import java.io.Serializable;

/**
 * @author miao
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MessageSendDto<T> implements Serializable {
    private Long messageId;
    private String sessionId;
    private String sendUserId;
    private String sendUserNickName;
    private String contactId;
    private String contactName;
    private String messageContent;
    private String lastMessage;
    private Integer messageType;
    private Long sendTime;
    private Integer contactType;
    //扩展信息
    private T extendData;
    //0 正在发送  1  已经发送
    private Integer status;
    //文件信息

    private Long fileSize;
    private String fileName;
    private Integer fileType;
    //群员
    private Integer memberCount;

    public String getLastMessage() {
        if(StringUtils.isEmpty(lastMessage)){
            return messageContent;
        }
        return lastMessage;
    }
}
