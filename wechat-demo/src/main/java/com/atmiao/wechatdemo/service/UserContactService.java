package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.dto.UserContactSearchResultDto;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author musichao
* @description 针对表【user_contact】的数据库操作Service
* @createDate 2024-06-12 18:28:24
*/
public interface UserContactService extends IService<UserContact> {
    void addContact(String applyUserId,String receiveUserId,String contactId,Integer contactType,String applyInfo);

    UserContact valid(String userId, String groupId);

    Integer getGroupNumber(String groupId);

    List<UserContact> queryGroups(GroupInfo groupInfo);

    UserContactSearchResultDto searchContact(String userId, String contactId);

    Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo);

    List<UserContact> loadContact(String userId, String contactType);

    void removeUserContact(String userId, String contactId, UserContactStatusEnum userContactStatusEnum);
}
