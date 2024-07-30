package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.pojo.ChatSessionUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author musichao
* @description 针对表【chat_session_user】的数据库操作Service
* @createDate 2024-06-27 11:14:53
*/
public interface ChatSessionUserService extends IService<ChatSessionUser> {
        //联合主键
         void saveOrUpdateUser(ChatSessionUser chatSessionUser);
         //更新冗余
         void updateRedundantInfo(String contactNameUpdate,String contactId);
}
