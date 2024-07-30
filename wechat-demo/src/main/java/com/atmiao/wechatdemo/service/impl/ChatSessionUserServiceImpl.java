package com.atmiao.wechatdemo.service.impl;

import com.atmiao.wechatdemo.commons.enums.MessageTypeEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.mapper.UserContactMapper;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.websosket.netty.MessageHandler;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.ChatSessionUser;
import com.atmiao.wechatdemo.service.ChatSessionUserService;
import com.atmiao.wechatdemo.mapper.ChatSessionUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author musichao
 * @description 针对表【chat_session_user】的数据库操作Service实现
 * @createDate 2024-06-27 11:14:53
 */
@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser>
        implements ChatSessionUserService {
    @Autowired
    private ChatSessionUserMapper chatSessionUserMapper;
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private UserContactMapper userContactMapper;

    @Override
    public void saveOrUpdateUser(ChatSessionUser chatSessionUser) {
        //联合主键
        ChatSessionUser sessionUser = chatSessionUserMapper.queryOneByUserIdAndContactId(chatSessionUser.getUserId(), chatSessionUser.getContactId());
        if (sessionUser == null) {
            chatSessionUserMapper.insert(chatSessionUser);
        } else {
            LambdaUpdateWrapper<ChatSessionUser> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ChatSessionUser::getUserId, chatSessionUser.getUserId()).eq(ChatSessionUser::getContactId, chatSessionUser.getContactId());
            chatSessionUserMapper.update(chatSessionUser, wrapper);
        }
    }

    @Override
    public void updateRedundantInfo(String contactNameUpdate, String contactId) {
        chatSessionUserMapper.updateContactNameByContactId(contactNameUpdate, contactId);
        //如果是用户修改，需要遍历用户的所有好友，获得所有contactId
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByPrefix(contactId);
        if (contactTypeEnum == null) {
            return;
        }
        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
        messageSendDto.setExtendData(contactNameUpdate);
        // 修改群名称发送ws消息
        if (contactTypeEnum == UserContactTypeEnum.GROUP) {
            messageSendDto.setContactType(contactTypeEnum.getType());
            messageSendDto.setContactId(contactId);
            messageHandler.sendMessage(messageSendDto);
        } else {
            List<UserContact> userContactList = userContactMapper.queryAllByContactIdAndContactTypeAndStatus(contactId, UserContactTypeEnum.USER.getType(), UserContactStatusEnum.FRIEND.getStatus());
            messageSendDto.setContactType(contactTypeEnum.getType());
            if (!userContactList.isEmpty()) {
                for (UserContact userContact : userContactList) {
                    messageSendDto.setContactId(userContact.getUserId());
                    messageSendDto.setSendUserId(contactId);
                    messageSendDto.setSendUserNickName(contactNameUpdate);
                    messageHandler.sendMessage(messageSendDto);
                }
            }
        }
    }
}




