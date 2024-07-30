package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.commons.enums.MessageTypeEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.pojo.ChatMessage;
import com.atmiao.wechatdemo.pojo.GroupMFVo;
import com.atmiao.wechatdemo.service.ChatMessageService;
import com.atmiao.wechatdemo.service.ChatSessionService;
import com.atmiao.wechatdemo.service.ChatSessionUserService;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.atmiao.wechatdemo.utils.StreamChangeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author miao
 * @version 1.0
 */
@RestController("chatController")
@CrossOrigin
@Slf4j
@Tag(name = "聊天模块", description = "好友聊天等接口")
@RequestMapping("chat")
@Validated
public class ChatController {
    @Autowired
    private ChatSessionUserService chatSessionUserService;
    @Autowired
    private ChatSessionService chatSessionService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RedisComponent redisComponent;

    @Operation(summary = "sendMessage", description = "发送消息的方法")
    @PostMapping("sendMessage")
    @GlobalInterceptor
    public ResponseVo sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty @Size(max = 500,message = "Message content must not exceed 500 characters") String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) {

        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageContent(messageContent);
        chatMessage.setContactId(contactId);
        chatMessage.setMessageType(messageType);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        MessageSendDto messageSendDto = chatMessageService.saveMessage(chatMessage, tokenUserInfoDto);
        return ResponseVo.getSuccessResponseVo(messageSendDto);
    }

    @Operation(summary = "uploadFile", description = "发送文件消息的方法")
    @PostMapping("uploadFile")
    @GlobalInterceptor
    public ResponseVo uploadFile(HttpServletRequest request,
                                 @NotNull Long messageId,
                                 @NotNull MultipartFile file,
                                 MultipartFile cover) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        chatMessageService.saveMessageFile(tokenUserInfoDto.getUserId(), messageId, file, cover);
        return ResponseVo.getSuccessResponseVo(null);
    }

    @Operation(summary = "downloadFile", description = "下载文件消息的方法")
    @PostMapping("downloadFile")
    @GlobalInterceptor
    public void downloadFile(HttpServletRequest request, HttpServletResponse response,
                                   @NotEmpty String fileId,
                                   @NotNull Boolean showCover) throws Exception {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        FileInputStream in = null;
        OutputStream out = null;
        try {
            File file = null;
            if(!CommonUtils.isNumber(fileId)){
                String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
                String avatarPath = appConfig.getProjectFolder() + avatarFolderName + fileId+Constants.IMAGE_SUFFIX;
                if(showCover){
                    avatarPath = avatarPath + Constants.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if(!file.exists()){
                    throw new BusinessException(ResponseStatusCode.CODE_602);
                }

            }else {
                file = chatMessageService.downloadFile(tokenUserInfoDto,Long.parseLong(fileId),showCover);
            }
            response.setContentType("application/x-msdownload;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;");
            response.setContentLengthLong(file.length());
            in = new FileInputStream(file);
            byte[] bytes = StreamChangeUtil.streamToByteArray(in);
            out = response.getOutputStream();
            out.write(bytes);
            out.flush();

        } catch (Exception e) {
           log.error("下载文件失败");
            throw e;
        } finally {
            //先开后关
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                   log.error("io异常",e);
                }
            }
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("io异常",e);
                }
            }
        }
    }
}
