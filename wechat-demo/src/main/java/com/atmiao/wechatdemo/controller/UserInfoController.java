package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.dto.UserContactSearchResultDto;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.pojo.UserInfoVo;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.atmiao.wechatdemo.utils.CopyUtil;
import com.atmiao.wechatdemo.utils.MD5Util;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.atmiao.wechatdemo.websosket.ChannelContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author miao
 * @version 1.0
 */
@RestController
@CrossOrigin
@Slf4j
@RequestMapping("userInfo")
@Tag(name = "好友信息模块",description = "查看好友信息")
@Validated
public class UserInfoController {
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private ChannelContextUtils channelContextUtils;
    @Operation(summary = "getUserInfo", description = "获取用户信息")
    @PostMapping("getUserInfo")
    @GlobalInterceptor
    public ResponseVo getUserInfo(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        UserInfo userInfo = userInfoService.getById(tokenUserInfoDto.getUserId());
        UserInfoVo userInfoVo = CopyUtil.copy(userInfo, UserInfoVo.class);
        userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());
        return ResponseVo.getSuccessResponseVo(userInfoVo);
    }
    @Operation(summary = "saveUserInfo", description = "保存用户信息")
    @PostMapping("saveUserInfo")
    @GlobalInterceptor
    public ResponseVo saveUserInfo(HttpServletRequest request, UserInfo userInfo,
                                   @RequestParam(value = "avatarFile",required = false) MultipartFile avatarFile,
                                   @RequestParam(value = "avatarCover",required = false) MultipartFile avatarCover) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfoService.saveUserInfo(userInfo,avatarFile,avatarCover);
        return getUserInfo(request);
    }
    @Operation(summary = "updatePassword", description = "修改密码")
    @PostMapping("updatePassword")
    @GlobalInterceptor
    public ResponseVo updatePassword(HttpServletRequest request,@RequestParam("password")
                                    @Pattern(regexp = Constants.REGEX_PASSWORD) String password) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setPassword(MD5Util.encrypt(password));
        userInfoService.updateById(userInfo);
        // 强制退出，重新登陆
        channelContextUtils.closeContact(tokenUserInfoDto.getUserId());
        return ResponseVo.getSuccessResponseVo(null);
    }
    @Operation(summary = "logout", description = "退出登录")
    @PostMapping("logout")
    @GlobalInterceptor
    public ResponseVo logout(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        // 退出登陆，关闭ws连接
        channelContextUtils.closeContact(tokenUserInfoDto.getUserId());
        return ResponseVo.getSuccessResponseVo(null);
    }

}
