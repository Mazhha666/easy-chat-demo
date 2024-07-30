package com.atmiao.wechatdemo.websosket;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.enums.MessageTypeEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactApplyStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.dto.WsInitData;
import com.atmiao.wechatdemo.mapper.*;
import com.atmiao.wechatdemo.pojo.*;
import com.atmiao.wechatdemo.utils.JsonUtils;
import com.atmiao.wechatdemo.utils.RedisComponent;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author miao
 * @version 1.0
 */
@Component
@Slf4j
public class ChannelContextUtils {
    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private ChatSessionUserMapper chatSessionUserMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private UserContactApplyMapper userContactApplyMapper;

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        log.info("channelId:{}", channelId);
        AttributeKey attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
        //加载redis里所有联系人
        //List<UserContact> contactList = redisComponent
        List<String> userContactList = redisComponent.getUserContactList(userId);
        for (String contact : userContactList) {
            if (contact.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                add2Group(contact, channel);
            }
        }
        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveUserHeartBeat(userId);
        //更新用户最后登陆状态
        userInfoMapper.updateLastLoginTimeByUserId(LocalDateTime.now(), userId);
        //给用户发送消息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Long sourceLastOffTime = userInfo.getLastOffTime();
        Long lastOffTime = sourceLastOffTime;
        if (sourceLastOffTime == null || System.currentTimeMillis() - Constants.MILLISSECONDS_3DAYS_AGC > sourceLastOffTime) {
            //只保留三天以内的
            lastOffTime = System.currentTimeMillis() - Constants.MILLISSECONDS_3DAYS_AGC;
        }
        //查询会话信息
        List<ChatSessionUser> chatSessionUserList = chatSessionUserMapper.querySessionUserList(userId);
        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUserList);
        //查询聊天信息 群聊 + 自己 contact
        List<String> groupList = userContactList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        groupList.add(userId);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactIdList(groupList);
        chatMessage.setLastReceiveTime(lastOffTime);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectMessageList(chatMessage);
        wsInitData.setChatMessageList(chatMessageList);
        //查询好友申请
        UserContactApply userContactApply = new UserContactApply();
        userContactApply.setReceiveUserId(userId);
        userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        userContactApply.setLastApplyTimeStamp(lastOffTime);
        List<UserContactApply> userContactApplyList = userContactApplyMapper.queryInThreeDaysApply(userContactApply);
        Integer applyCount = userContactApplyList.size();
        wsInitData.setApplyCount(applyCount);
        //发送消息
        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);
        sendMsg(messageSendDto, userId);
    }

    /**
     * 这个只是自查的 和 send2User send2Group 不一样
     *
     * @param messageSendDto
     * @param receiveId
     */
    public void sendMsg(MessageSendDto messageSendDto, String receiveId) {
        if (receiveId == null) {
            return;
        }
        Channel sendChannel = USER_CONTEXT_MAP.get(receiveId);
        if (sendChannel == null) {
            return;
        }
        System.out.println("消息类型为"+ messageSendDto.getMessageType());
        //相对于客户端而言，联系人就是发送人，所以转一下再发送,请求不处理
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())) {
            //获得的是申请人申请的联系人的信息
            System.out.println("===================Jinrukew===");
            UserInfo userInfo = (UserInfo) messageSendDto.getExtendData();
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDto.setContactId(userInfo.getUserId());
            messageSendDto.setContactName(userInfo.getNickName());
            messageSendDto.setExtendData(null);
        } else {
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        }
        sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.toJSONString(messageSendDto)));
    }

    private void add2Group(String groupId, Channel channel) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if (channel == null) {
            return;
        }
        group.add(channel);
    }
    public void addUser2Group(String userId,String groupId){
        if(StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userId)){
            return;
        }
        Channel channel = USER_CONTEXT_MAP.get(userId);
        add2Group(groupId,channel);
    }
    public void removeContext(Channel channel) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if (!StringUtils.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeUserHeartBeat(userId);
        //更新用户最后离线时间
        userInfoMapper.updateLastOffTimeByUserId(new Date().getTime(), userId);
    }

    public void sendMessageByType(MessageSendDto messageSendDto) {
        //根据contactId区分
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByPrefix(messageSendDto.getContactId());
        if (contactTypeEnum == null) {
            return;
        }
        switch (contactTypeEnum) {
            case USER -> {
                send2User(messageSendDto);
            }
            case GROUP -> {
                send2Group(messageSendDto);
            }
        }
    }

    public void closeContact(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        redisComponent.cleanUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null) {
            return;
        }
        channel.close();
    }

    private void send2User(MessageSendDto messageSendDto) {
        if (StringUtils.isEmpty(messageSendDto.getContactId())) {
            return;
        }
        String contactId = messageSendDto.getContactId();
        sendMsg(messageSendDto, contactId);
        //强制下线
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())) {
            closeContact(contactId);
        }
    }

    private void send2Group(MessageSendDto messageSendDto) {
        if (StringUtils.isEmpty(messageSendDto.getContactId())) {
            return;
        }
        ChannelGroup group = GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if (group == null) {
            return;
        }
        group.writeAndFlush(new TextWebSocketFrame(JsonUtils.toJSONString(messageSendDto)));
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDto.getMessageType());
        if(MessageTypeEnum.REMOVE_GROUP == messageTypeEnum || MessageTypeEnum.LEAVE_GROUP == messageTypeEnum){
            //移除群组和用户的频道
            String userId = (String) messageSendDto.getExtendData();
            Channel channel = USER_CONTEXT_MAP.get(userId);
            if(channel == null){
                return;
            }
            group.remove(channel);
        }
        if(MessageTypeEnum.DISSOLUTION_GROUP == messageTypeEnum){
            GROUP_CONTEXT_MAP.remove(messageSendDto.getContactId());
            group.close();
        }
    }


}
