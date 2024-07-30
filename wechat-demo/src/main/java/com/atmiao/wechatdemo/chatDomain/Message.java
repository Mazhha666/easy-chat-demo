package com.atmiao.wechatdemo.chatDomain;

import lombok.Data;
import lombok.ToString;

/**
 * @author miao
 * @version 1.0
 */
@Data
@ToString
public class Message {
    private String role;
    private String content;
}
