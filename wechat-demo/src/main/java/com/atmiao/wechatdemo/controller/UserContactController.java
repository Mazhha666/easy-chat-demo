package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.dto.UserContactSearchResultDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.pojo.UserContactApply;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.pojo.UserInfoVo;
import com.atmiao.wechatdemo.service.UserContactApplyService;
import com.atmiao.wechatdemo.service.UserContactService;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.atmiao.wechatdemo.utils.CopyUtil;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author miao
 * @version 1.0
 */
@RestController
@CrossOrigin
@Slf4j
@RequestMapping("contact")
@Tag(name = "添加好友模块", description = "添加好友功能的实现")
@Validated
public class UserContactController {
    @Autowired
    private UserContactService userContactService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserContactApplyService userContactApplyService;
    @Autowired
    private RedisComponent redisComponent;

    @Operation(summary = "search", description = "搜索群组或好友")
    @GetMapping("search")
    @GlobalInterceptor
    public ResponseVo search(HttpServletRequest request, @RequestParam("contactId") @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        UserContactSearchResultDto resultDto = userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);
        return ResponseVo.getSuccessResponseVo(resultDto);
    }

    @Operation(summary = "applyAdd", description = "申请添加的方法")
    @GetMapping("applyAdd")
    @GlobalInterceptor
    public ResponseVo applyAdd(HttpServletRequest request,
                               @RequestParam("contactId") @NotEmpty String contactId,
                               @RequestParam("applyInfo") @NotEmpty String applyInfo) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        //JOINTYPE 需要同意才能加入或者直接加入
        Integer joinType = userContactService.applyAdd(tokenUserInfoDto, contactId, applyInfo);
        return ResponseVo.getSuccessResponseVo(joinType);
    }

    @Operation(summary = "loadApply", description = "加载申请信息的方法")
    @GetMapping("loadApply")
    @GlobalInterceptor
    public ResponseVo loadApply(HttpServletRequest request,@RequestParam("pageNo") Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        Page<UserContactApply> page = userContactApplyService.loadApply(tokenUserInfoDto, pageNo);
        return ResponseVo.getSuccessResponseVo(page);
    }

    @Operation(summary = "dealWithApply", description = "处理申请信息的方法")
    @GetMapping("dealWithApply")
    @GlobalInterceptor
    public ResponseVo dealWithApply(HttpServletRequest request, @RequestParam("applyId")@NotNull Integer applyId, @RequestParam("status")@NotNull Integer status) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        userContactApplyService.delWith(tokenUserInfoDto.getUserId(), applyId, status);
        return ResponseVo.getSuccessResponseVo(null);
    }

    @Operation(summary = "loadContact", description = "加载好友和群聊方法")
    @GetMapping("loadContact")
    @GlobalInterceptor
    public ResponseVo loadContact(HttpServletRequest request,@RequestParam("contactType") @NotNull String contactType) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        List<UserContact> contacts = userContactService.loadContact(tokenUserInfoDto.getUserId(), contactType);
        return ResponseVo.getSuccessResponseVo(contacts);
    }

    /**
     *
     * @param request 获取联系人，不一定是好友 群聊中查询
     * @param contactId
     * @return
     */
    @Operation(summary = "getContactInfo", description = "加载好友和群聊详细信息")
    @GetMapping("getContactInfo")
    @GlobalInterceptor
    public ResponseVo getContactInfo(HttpServletRequest request, @RequestParam("contactId")@NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        UserInfo userInfo = userInfoService.getById(contactId);
        UserInfoVo userInfoVo = CopyUtil.copy(userInfo, UserInfoVo.class);
        userInfoVo.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        //检验非程序接口
        UserContact contact = userContactService.valid(tokenUserInfoDto.getUserId(), contactId);
        if (contact != null) {
            userInfoVo.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }
        return ResponseVo.getSuccessResponseVo(userInfoVo);
    }

    /**
     *
     * @param request 一定是联系人
     * @param contactId
     * @return
     */

    @Operation(summary = "getContactUserInfo", description = "加载好友详细信息")
    @GetMapping("getContactUserInfo")
    @GlobalInterceptor
    public ResponseVo getContactUserInfo(HttpServletRequest request, @RequestParam("contactId")@NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        UserContact contact = userContactService.valid(tokenUserInfoDto.getUserId(), contactId);
        if (contact == null || !ArrayUtils.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus()
        }, contact.getStatus())) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        UserInfo userInfo = userInfoService.getById(contactId);
        UserInfoVo userInfoVo = CopyUtil.copy(userInfo, UserInfoVo.class);
        return ResponseVo.getSuccessResponseVo(userInfoVo);
    }
    @Operation(summary = "delContact", description = "删除联系人")
    @GetMapping("delContact")
    @GlobalInterceptor
    public ResponseVo delContact(HttpServletRequest request,@RequestParam("contactId") @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.DEL);
        return ResponseVo.getSuccessResponseVo(null);
    }
    @Operation(summary = "addContact2BlackList", description = "拉黑联系人")
    @GetMapping("addContact2BlackList")
    @GlobalInterceptor
    public ResponseVo addContact2BlackList(HttpServletRequest request, @RequestParam("contactId")@NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.BLACKLIST);
        return ResponseVo.getSuccessResponseVo(null);
    }
}

