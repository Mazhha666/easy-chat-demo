package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.pojo.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
* @author musichao
* @description 针对表【chat_message】的数据库操作Service
* @createDate 2024-06-27 11:14:32
*/
public interface ChatMessageService extends IService<ChatMessage> {
    MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);
    void saveMessageFile(String userId, Long messageId, MultipartFile file,MultipartFile cover);
    File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long fileId, Boolean showCover);
}
