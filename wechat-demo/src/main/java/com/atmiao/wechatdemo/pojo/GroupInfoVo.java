package com.atmiao.wechatdemo.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author miao
 * @version 1.0
 */
@Data
public class GroupInfoVo {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;
}
