package com.atmiao.wechatdemo.dto;

import com.atmiao.wechatdemo.pojo.ChatMessage;
import com.atmiao.wechatdemo.pojo.ChatSessionUser;
import lombok.Data;

import java.util.List;

/**
 * @author miao
 * @version 1.0
 */
@Data
public class WsInitData {
    private List<ChatSessionUser> chatSessionList;
    private List<ChatMessage> chatMessageList;
    private Integer applyCount;
}
