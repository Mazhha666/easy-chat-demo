package com.atmiao.wechatdemo.chatDomain;

import lombok.Data;
import lombok.ToString;

import java.util.List;


/**
 * @author miao
 * @version 1.0
 */
@Data
@ToString
public class ChatResponse {
    private Integer code;
    private String message;
    private String sid;
    private List<Choice> choices;
    private Usage usage;
}
