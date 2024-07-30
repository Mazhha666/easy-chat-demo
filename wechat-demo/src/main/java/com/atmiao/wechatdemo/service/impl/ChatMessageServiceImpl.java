package com.atmiao.wechatdemo.service.impl;

import com.alibaba.fastjson2.util.DateUtils;
import com.atmiao.wechatdemo.chatDomain.ChatResponse;
import com.atmiao.wechatdemo.chatDomain.Choice;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.enums.*;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.mapper.ChatSessionMapper;
import com.atmiao.wechatdemo.mapper.UserContactMapper;
import com.atmiao.wechatdemo.pojo.ChatSession;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.service.ChatService;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.CopyUtil;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.atmiao.wechatdemo.websosket.netty.MessageHandler;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.ChatMessage;
import com.atmiao.wechatdemo.service.ChatMessageService;
import com.atmiao.wechatdemo.mapper.ChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author musichao
 * @description 针对表【chat_message】的数据库操作Service实现
 * @createDate 2024-06-27 11:14:32
 */
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private UserContactMapper userContactMapper;
    @Autowired
    private ChatService chatService;
    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())) {
            List<String> userContactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
            if (!userContactList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByPrefix(chatMessage.getContactId());
                if (UserContactTypeEnum.USER == contactTypeEnum) {
                    throw new BusinessException(ResponseStatusCode.CODE_902);
                } else {
                    throw new BusinessException(ResponseStatusCode.CODE_903);
                }
            }
        }
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if (null == messageTypeEnum || !ArrayUtils.contains(new Integer[]{
                MessageTypeEnum.CHAT.getType(),
                MessageTypeEnum.MEDIA_CHAT.getType()
        }, messageTypeEnum.getType())) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        String sessionId = null;
        String userId = tokenUserInfoDto.getUserId();
        String contactId = chatMessage.getContactId();
        Long curTime = System.currentTimeMillis();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByPrefix(contactId);
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        if (contactTypeEnum == UserContactTypeEnum.USER) {
            sessionId = CommonUtils.getChatSessionId(new String[]{userId, contactId});
        } else {
            sessionId = CommonUtils.getChatSessionId4Group(contactId);
        }
        String messageContent = CommonUtils.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setSendTime(curTime);
        chatMessage.setSessionId(sessionId);
        chatMessage.setStatus(status);
        chatMessage.setSendUserId(userId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setMessageContent(messageContent);
        chatMessage.setContactType(contactTypeEnum.getType());

        //更新会话
        ChatSession chatSession = new ChatSession();
        chatSession.setLastReceiveTime(curTime);
        chatSession.setLastMessage(messageContent);
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            chatSession.setLastMessage(userId + ":" + messageContent);
        }
        chatSession.setSessionId(sessionId);
        chatSessionMapper.updateById(chatSession);
        //记录消息表
        chatMessageMapper.insert(chatMessage);
        //发送消息
        MessageSendDto messageSendDto = CopyUtil.copy(chatMessage, MessageSendDto.class);
        //机器人好友回送接口
        if (Constants.ROBOT_UID.equals(contactId)) {
            messageContent = messageContent + ",回复字数不能多于500,越简单明了越好，最好在100字以内";
            Mono<ChatResponse> chatCompletion = chatService.getChatCompletion("4.0Ultra", messageContent);
            //异步调用
            chatCompletion.subscribe(
                    chatResponse -> {
                        StringBuilder sb = new StringBuilder();
                        for (Choice choice : chatResponse.getChoices()) {
                            String content = choice.getMessage().getContent();
                            sb.append(content+" ");
                        }
                        SysSettingDto sysSetting = redisComponent.getSysSetting();
                        TokenUserInfoDto robot = new TokenUserInfoDto();
                        robot.setUserId(sysSetting.getRobotUid());
                        robot.setNickName(sysSetting.getRobotNickName());
                        ChatMessage robotMessage = new ChatMessage();
                        robotMessage.setContactId(userId);
                        //对接ai
                        robotMessage.setMessageContent(sb.toString());
                        robotMessage.setMessageType(MessageTypeEnum.CHAT.getType());
                        saveMessage(robotMessage, robot);
                    },
                    error -> System.err.println("Error: " + error.getMessage()),
                    () -> System.out.println("Completed")
            );
        }else{
            messageHandler.sendMessage(messageSendDto);
        }
        return messageSendDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if (!userId.equals(chatMessage.getSendUserId())) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        SysSettingDto sysSetting = redisComponent.getSysSetting();
        String fileSuffix = CommonUtils.getFileSuffix(file.getOriginalFilename());
//老罗写法
        //        if (!StringUtils.isEmpty(fileSuffix) &&
//                ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
//                && file.getSize() > sysSetting.getMaxImageSize() * Constants.FILE_SIZE_MB
//        ) {
//            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
//        } else if (!StringUtils.isEmpty(fileSuffix) &&
//                ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
//                && file.getSize() > sysSetting.getMaxVideoSize() * Constants.FILE_SIZE_MB) {
//            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
//        } else if(!StringUtils.isEmpty(fileSuffix) &&
//                !ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase() )
//                && !ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
//                &&file.getSize() > sysSetting.getMaxFileSize() * Constants.FILE_SIZE_MB){
//            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
//        }
//我的写法
        if (StringUtils.isEmpty(fileSuffix)) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }

        //图片，视频，或者其他文件
        if (file.getSize() > sysSetting.getMaxFileSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if (ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSetting.getMaxImageSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if (ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSetting.getMaxVideoSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        String fileName = file.getOriginalFilename();
        String fileExtName = CommonUtils.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE +month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);
        try {
            file.transferTo(uploadFile);
            //不是文件的话
            if(cover != null){
                cover.transferTo(new File(uploadFile + Constants.COVER_IMAGE_SUFFIX));
            }

        } catch (IOException e) {
            log.error("上传文件失败");
            throw new BusinessException("文件长传失败");
        }
        //更新消息 乐观锁 status
        chatMessageMapper.updateStatusByMessageIdAndStatus(MessageStatusEnum.SENDED.getStatus(), messageId, MessageStatusEnum.SENDING.getStatus());
        //发送消息
        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSendDto.setMessageId(messageId);
        messageSendDto.setContactId(chatMessage.getContactId());
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageHandler.sendMessage(messageSendDto);

    }

    @Override
    public File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long messageId, Boolean showCover) {
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        String contactId = chatMessage.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByPrefix(contactId);
        if (UserContactTypeEnum.USER == contactTypeEnum && !tokenUserInfoDto.getUserId().equals(contactId)) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            //查询是不是群组内的
            UserContact userContact = userContactMapper.queryOneByUserIdAndContactIdAndContactTypeAndStatus(tokenUserInfoDto.getUserId(),
                    chatMessage.getContactId(), UserContactTypeEnum.GROUP.getType(), UserContactStatusEnum.FRIEND.getStatus());
            if (userContact == null) {
                throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
            }
        }
        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = chatMessage.getFileName();
        String fileExtName = CommonUtils.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        if (showCover != null && showCover) {
            fileRealName = fileRealName + Constants.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + File.separator + fileRealName);
        if (!file.exists()) {
            log.info("文件不存在{}", messageId);
            throw new BusinessException(ResponseStatusCode.CODE_602);
        }
        return file;
    }

}




