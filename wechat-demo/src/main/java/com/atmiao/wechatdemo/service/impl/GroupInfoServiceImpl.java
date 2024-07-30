package com.atmiao.wechatdemo.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.enums.*;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.mapper.*;
import com.atmiao.wechatdemo.pojo.*;
import com.atmiao.wechatdemo.service.ChatSessionService;
import com.atmiao.wechatdemo.service.ChatSessionUserService;
import com.atmiao.wechatdemo.service.UserContactService;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.CopyUtil;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.atmiao.wechatdemo.websosket.ChannelContextUtils;
import com.atmiao.wechatdemo.websosket.netty.MessageHandler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.service.GroupInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author musichao
 * @description 针对表【group_info】的数据库操作Service实现
 * @createDate 2024-06-12 18:28:24
 */
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
        implements GroupInfoService {
    @Autowired
    GroupInfoMapper groupInfoMapper;
    @Autowired
    RedisComponent redisComponent;
    @Autowired
    UserContactMapper userContactMapper;
    @Autowired
    AppConfig appConfig;
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private ChatSessionUserMapper chatSessionUserMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    //这个只能单服务器发送
    @Autowired
    private ChannelContextUtils channelContextUtils;
    //redisson 的redis CLIENT
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private ChatSessionService chatSessionService;
    @Autowired
    private ChatSessionUserService chatSessionUserService;
    @Autowired
    private UserContactService userContactService;
    @Autowired
    private UserInfoMapper userInfoMapper;
    //用于处理内部事务调用，不然transactional不管用 lazy避免循环依赖
    @Autowired
    @Lazy
    private GroupInfoService groupInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveGroup(GroupMFVo groupMFVo, TokenUserInfoDto tokenUserInfoDto) {
        //封装
        GroupInfo groupInfo = saveGroupInfo(groupMFVo, tokenUserInfoDto);
        //没有就新增，有就修改
        if (StringUtils.isEmpty(groupMFVo.getGroupId())) {
            LocalDateTime now = LocalDateTime.now();
            //查询创建人以及已经创建了多少群组，不能超过SysSettingDto的上限
            LambdaQueryWrapper<GroupInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupInfo::getGroupOwnerId, tokenUserInfoDto.getUserId());
            int count = groupInfoMapper.selectCount(wrapper).intValue();
            SysSettingDto sysSetting = redisComponent.getSysSetting();
            if (count >= sysSetting.getMaxGroupCount()) {
                throw new BusinessException("最多创建" + sysSetting.getMaxGroupCount() + "个群聊");
            }
//            if (null == groupMFVo.getAvatarFile()) {
//                throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
//            }
            //保存并插入数据库
            groupInfo.setCreateTime(now);
            //赋予groupId
            groupInfo.setGroupId(CommonUtils.getGroupId());
            groupInfoMapper.insert(groupInfo);
            //将群组添加为联系人
            UserContact userContact = new UserContact();
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setCreateTime(now);
            userContact.setLastUpdateTime(now) ;
            userContactMapper.insert(userContact);
            // 创建会话
            ChatSession chatSession = new ChatSession();
            String sessionId = CommonUtils.getChatSessionId4Group(groupInfo.getGroupId());
            chatSession.setSessionId(sessionId);
            chatSession.setLastReceiveTime(new Date().getTime());
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionService.saveOrUpdate(chatSession);
            //会话好友
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setSessionId(sessionId);
            chatSessionUserMapper.insert(chatSessionUser);
            //会话消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            //群聊不用设置了
//            chatMessage.setSendUserNickName(groupInfo.getGroupOwnerNickName());
//            chatMessage.setSendUserId(groupInfo.getGroupOwnerId());
            chatMessage.setSendTime(new Date().getTime());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            chatMessageMapper.insert(chatMessage);
            //redis的list继续push好友
            redisComponent.addUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
            //netty的群组加入群主的channel
            channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(),groupInfo.getGroupId());
            // 发送ws消息
            chatSessionUser.setLastReceiveTime(new Date().getTime());
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setMemberCount(1);
            MessageSendDto messageSendDto = CopyUtil.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setExtendData(chatSessionUser);
            messageSendDto.setLastMessage(chatSessionUser.getLastMessage());
            messageHandler.sendMessage(messageSendDto);
        } else {
            //校验token所得的userid所拥有的群组与所传入groupId是否包含，避免传入其他人的groupId
            GroupInfo dbInfo = groupInfoMapper.selectById(groupInfo.getGroupId());
            if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
            }
            this.groupInfoMapper.updateById(groupInfo);
            // 更新相关表冗余信息
            String contactNameUpdate = null;
            if(!dbInfo.getGroupName().equals(groupInfo.getGroupName())){
                contactNameUpdate = groupInfo.getGroupName();
            }
            if(contactNameUpdate != null){
                chatSessionUserService.updateRedundantInfo(contactNameUpdate, groupInfo.getGroupId());
            }
        }
        if (null == groupMFVo.getAvatarFile()) {
            return;
        }
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if(!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getGroupId() + Constants.IMAGE_SUFFIX;
        try {
            //保存
            groupMFVo.getAvatarFile().transferTo(new File(filePath));
            groupMFVo.getAvatarCover().transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GroupInfo> loadMyGroup(String userId) {
        LambdaQueryWrapper<GroupInfo> wrapper = new LambdaQueryWrapper<>();
        // 解散的不查询还是保留 .eq(GroupInfo::getStatus,GroupStatusEnum.NOMAL.getStatus())
        wrapper.eq(GroupInfo::getGroupOwnerId,userId).orderByDesc(GroupInfo::getCreateTime).eq(GroupInfo::getStatus,GroupStatusEnum.NOMAL.getStatus());
        List<GroupInfo> groupInfoList = groupInfoMapper.selectList(wrapper);
        return groupInfoList;
    }

    @Override
    public Page<GroupInfo> loadGroup() {
        //TODO 后续修改size 不然数量还是0
        Page<GroupInfo> page = new Page<>(1, 20);
//        LambdaQueryWrapper<GroupInfo> wrapper = new LambdaQueryWrapper<>();
//        wrapper.orderByDesc(GroupInfo::getCreateTime);
//        Page<GroupInfo> groupInfoPage = groupInfoMapper.selectPage(page, wrapper);
       //TODO 考虑群组拉黑几种状态是否影响查询结果准确性,
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setQueryGroupOwnerNickName(true);
        groupInfo.setQueryMemberCount(true);
        Page<GroupInfo> groupInfoPage = groupInfoMapper.loadGroup(page, groupInfo);
        return groupInfoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void distributeGroup(String groupOwnerId, String groupId) {
        GroupInfo dbInfo = groupInfoMapper.selectById(groupId);
        if(groupId == null || !dbInfo.getGroupOwnerId().equals(groupOwnerId)){
            //与缓存里的进行校验
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        //删除群组 TableLogic 逻辑删除
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
        groupInfo.setGroupId(groupId);
        groupInfoMapper.updateById(groupInfo);
        //删除群里的联系人
        UserContact userContact = new UserContact();
        userContact.setStatus(UserContactStatusEnum.DEL.getStatus());
        userContact.setLastUpdateTime(LocalDateTime.now());
        LambdaUpdateWrapper<UserContact> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserContact::getContactId,groupId).eq(UserContact::getContactType,UserContactTypeEnum.GROUP.getType());
        userContactMapper.update(userContact,wrapper);
        // 移除相关群员的联系人缓存
        List<UserContact> userContactList = userContactMapper.selectList(wrapper);
        for (UserContact contact : userContactList) {
            //每个联系人移除改群组
            redisComponent.removeUserContact(contact.getUserId(),groupId);
        }
        // 1.更新会话信息 2.记录群消息 3.发送消息
        ChatSession chatSession = new ChatSession();
        Date curDate = new Date();
        String sessionId = CommonUtils.getChatSessionId4Group(groupId);
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(curDate.getTime());
        String messageContent = MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();
        chatSession.setLastMessage(messageContent);
        chatSessionMapper.updateById(chatSession);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);
        MessageSendDto messageSendDto = CopyUtil.copy(chatMessage, MessageSendDto.class);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer opType) {
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if(null== groupInfo || !groupInfo.getGroupOwnerId().equals(tokenUserInfoDto.getUserId())){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        String[] contactList = selectContacts.split(",");
        for (String contact : contactList) {
            if(opType.equals(Constants.ZERO)){
                groupInfoService.leaveGroup(contact,groupId,MessageTypeEnum.REMOVE_GROUP);
            }else {
                userContactService.addContact(contact,null,groupId,UserContactTypeEnum.GROUP.getType(), null);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) {
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if(null == groupInfo){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if(userId.equals(groupInfo.getGroupOwnerId())){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        int  count = userContactMapper.delByUserIdAndContactId(userId, groupId);
        if(count == 0){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String sessionId = CommonUtils.getChatSessionId4Group(groupId);
        Date curDate = new Date();
        String messageContent = String.format(messageTypeEnum.getInitMessage(),userInfo.getNickName());
        //更新缓存
        redisComponent.removeUserContact(userId,groupId);
        //更新会话 用户自己退出需要删除会话
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSession.setSessionId(sessionId);
        chatSessionMapper.updateById(chatSession);
        //删除用户会话 可以保留。可以删除 我这里进行保留，也就是每次用户登录还会接受到信息
//        chatSessionUserMapper.delByUserIdAndContactId(userId,groupId);
        //保存消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setMessageContent(messageContent);
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessageMapper.insert(chatMessage);
        //更新人数
        LambdaQueryWrapper<UserContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserContact::getContactId,groupId).eq(UserContact::getStatus,UserContactStatusEnum.FRIEND.getStatus());
        int memberCount = userContactMapper.selectCount(wrapper).intValue();
        MessageSendDto messageSendDto = CopyUtil.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setExtendData(userId);
        messageSendDto.setMemberCount(memberCount );
        messageHandler.sendMessage(messageSendDto);

    }

    private GroupInfo saveGroupInfo(GroupMFVo groupMFVo, TokenUserInfoDto tokenUserInfoDto) {
        GroupInfo groupInfo = new GroupInfo();
        //有就是修改，没有就是新增
        groupInfo.setGroupId(groupMFVo.getGroupId());
        groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfo.setGroupName(groupMFVo.getGroupName());
        groupInfo.setGroupNotice(groupMFVo.getGroupNotice());
        groupInfo.setJoinType(groupMFVo.getJoinType());
        return groupInfo;
    }
}




