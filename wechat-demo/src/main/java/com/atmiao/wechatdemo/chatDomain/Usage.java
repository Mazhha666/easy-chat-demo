package com.atmiao.wechatdemo.chatDomain;

import lombok.Data;
import lombok.ToString;

/**
 * @author miao
 * @version 1.0
 */
@Data
@ToString
public class Usage {
    private Integer prompt_tokens;
    private Integer completion_tokens;
    private Integer total_tokens;
}
