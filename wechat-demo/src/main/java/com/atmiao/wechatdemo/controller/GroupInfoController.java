package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.commons.enums.GroupStatusEnum;
import com.atmiao.wechatdemo.commons.enums.MessageTypeEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.GroupInfoVo;
import com.atmiao.wechatdemo.pojo.GroupMFVo;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.service.GroupInfoService;
import com.atmiao.wechatdemo.service.UserContactService;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.RedisComponent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author miao
 * @version 1.0
 */
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("group")
@Tag(name = "群组模块",description = "用户群组好友功能的实现")
@Validated
public class GroupInfoController {
    @Autowired
    private GroupInfoService groupInfoService;
    @Autowired
    RedisComponent redisComponent;
    @Autowired
    private UserContactService userContactService;

    /**
     *
     * @param groupMFVo 一定要写requestbody 不然无法映射
     * @param result
     * @param request
     * @return
     */
    @Operation(summary = "saveGroup",description = "保存群组的方法")
    @PostMapping("saveGroup")
    @GlobalInterceptor
    public ResponseVo saveGroup(GroupMFVo groupMFVo, BindingResult result, HttpServletRequest request){
        if(result.hasErrors()){
            throw new BusinessException("群组名称不符合要求");
        }
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        groupInfoService.saveGroup(groupMFVo,tokenUserInfoDto);
        return ResponseVo.getSuccessResponseVo(null);

    }
    @Operation(summary = "loadMyGroup",description = "加载群组的方法")
    @PostMapping("loadMyGroup")
    @GlobalInterceptor
    public ResponseVo loadMyGroup(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
         //加载群组信息
        List<GroupInfo> groupInfoList = groupInfoService.loadMyGroup(tokenUserInfoDto.getUserId());
        return ResponseVo.getSuccessResponseVo(groupInfoList);
    }
    @Operation(summary = "getGroupInfo4Chat",description = "得到群聊会话群聊详情(查询群类用户多少人啥的） ")
    @PostMapping("getGroupInfo4Chat")
    @GlobalInterceptor
    public ResponseVo getGroupInfo4Chat(HttpServletRequest request,  @NotEmpty @RequestParam("groupId") String groupId){
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
        //查询列表
        List<UserContact> userContactList = userContactService.queryGroups(groupInfo);
        GroupInfoVo groupInfoVo = new GroupInfoVo();
        groupInfoVo.setGroupInfo(groupInfo);
        groupInfoVo.setUserContactList(userContactList);
        return ResponseVo.getSuccessResponseVo(groupInfoVo);

    }
    @Operation(summary = "getGroupInfo",description = "得到群组信息")
    @PostMapping("getGroupInfo")
    @GlobalInterceptor
    public ResponseVo getGroupInfo(HttpServletRequest request,  @NotEmpty @RequestParam("groupId") String groupId){
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
       //查询群组人数
        Integer count = userContactService.getGroupNumber(groupId);
        groupInfo.setMemberCount(count);
        return ResponseVo.getSuccessResponseVo(groupInfo);

    }
    @Operation(summary = "addOrRemoveGroupUser",description = "添加或移除群员信息")
    @PostMapping("addOrRemoveGroupUser")
    @GlobalInterceptor
    public ResponseVo addOrRemoveGroupUser(HttpServletRequest request,  @NotEmpty @RequestParam("groupId") String groupId,
                                           @NotEmpty String selectContacts,
                                           @NotNull Integer opType){
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto,groupId,selectContacts,opType);
        return ResponseVo.getSuccessResponseVo(null);

    }
    @Operation(summary = "leaveGroup",description = "离开群聊")
    @PostMapping("leaveGroup")
    @GlobalInterceptor
    public ResponseVo leaveGroup(HttpServletRequest request,  @NotEmpty @RequestParam("groupId") String groupId){
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(), groupId,MessageTypeEnum.LEAVE_GROUP);
        return ResponseVo.getSuccessResponseVo(null);

    }
    @Operation(summary = "dissolutionGroup",description = "解散群聊")
    @PostMapping("dissolutionGroup")
    @GlobalInterceptor
    public ResponseVo dissolutionGroup(HttpServletRequest request,  @NotEmpty @RequestParam("groupId") String groupId){
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        groupInfoService.distributeGroup(tokenUserInfoDto.getUserId(),groupId);
        return ResponseVo.getSuccessResponseVo(null);

    }
    private GroupInfo getGroupDetailCommon(HttpServletRequest request,  @NotEmpty @RequestParam("groupId") String groupId){
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        UserContact userContact = userContactService.valid(tokenUserInfoDto.getUserId(),groupId);
        if(null == userContact || !userContact.getStatus().equals(UserContactStatusEnum.FRIEND.getStatus())){
            throw new BusinessException("你不在群聊或者群聊不存在或已解散");
        }
        GroupInfo groupInfo = this.groupInfoService.getById(groupId);
        if(null == groupInfo || !GroupStatusEnum.NOMAL.getStatus().equals(groupInfo.getStatus())){
            throw new BusinessException("群聊不存在或已经解散");
        }
        return groupInfo;
    }
}
