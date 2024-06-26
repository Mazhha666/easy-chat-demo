package com.atmiao.wechatdemo.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.enums.*;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.dto.UserContactSearchResultDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.mapper.GroupInfoMapper;
import com.atmiao.wechatdemo.mapper.UserContactApplyMapper;
import com.atmiao.wechatdemo.mapper.UserInfoMapper;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.UserContactApply;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.service.UserContactApplyService;
import com.atmiao.wechatdemo.utils.CopyUtil;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.service.UserContactService;
import com.atmiao.wechatdemo.mapper.UserContactMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author musichao
 * @description 针对表【user_contact】的数据库操作Service实现
 * @createDate 2024-06-12 18:28:24
 */
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
        implements UserContactService {
    @Autowired
    private UserContactMapper userContactMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private UserContactApplyMapper userContactApplyMapper;
    @Autowired
    private UserContactApplyService userContactApplyService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserContactService userContactService;
    /**
     * @param userId  用于用户查询群组时进行校验，避免根据token获得的usesrid与传入的不一致，
     * @param groupId
     * @return
     */
    @Override
    public UserContact valid(String userId, String groupId) {
        LambdaQueryWrapper<UserContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserContact::getUserId, userId).eq(UserContact::getContactId, groupId);
        UserContact userContact = userContactMapper.selectOne(wrapper);
        return userContact;
    }

    @Override
    public Integer getGroupNumber(String groupId) {
        LambdaQueryWrapper<UserContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserContact::getContactId, groupId);
        Long nums = userContactMapper.selectCount(wrapper);
        return nums.intValue();
    }

    @Override
    public List<UserContact> queryGroups(GroupInfo groupInfo) {
        List<UserContact> userContactList = userContactMapper.queryGroups(groupInfo.getGroupId(), UserContactStatusEnum.FRIEND.getStatus(), true);
//        LambdaQueryWrapper<UserContact> wrapper = new LambdaQueryWrapper<>();
//        //默认UserContact 的 queryUserInfo 为真 关联查询 userInfo
//        wrapper.eq(UserContact::getContactId,groupInfo.getGroupId())
//                .eq(UserContact::getStatus, UserContactStatusEnum.FRIEND.getStatus()).orderByAsc(UserContact::getCreateTime);
//        List<UserContact> userContactList = userContactMapper.selectList(wrapper);
        return userContactList;
    }

    @Override
    public UserContactSearchResultDto searchContact(String userId, String contactId) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByPrefix(contactId);
        if (null == contactTypeEnum) {
            return null;
        }
        UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
        switch (contactTypeEnum) {
            case USER -> {
                UserInfo userInfo = userInfoMapper.selectById(userId);
                if (userInfo == null) {
                    return null;
                }
                resultDto = CopyUtil.copy(userInfo, UserContactSearchResultDto.class);
            }
            case GROUP -> {
                GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
                if (groupInfo == null) {
                    return null;
                }
                resultDto.setNickName(groupInfo.getGroupName());
            }
        }
        resultDto.setContactId(contactId);
        resultDto.setContactType(contactTypeEnum.toString());
        if (userId.equals(contactId)) {
            resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return resultDto;
        }
        //查询是否是好友
        UserContact userContact = userContactMapper.queryOneByUserIdAndContactId(userId, contactId);
        resultDto.setStatus(userContact == null ? null : userContact.getStatus());
//        resultDto.setStatusName(resultDto.getStatusName());
        return resultDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByPrefix(contactId);
        if (null == contactTypeEnum) {
            throw new BusinessException("查你妹,联系人不存在");
        }
        //申请人
        String applyUserId = tokenUserInfoDto.getUserId();
        //默认申请信息
        applyInfo = StringUtils.isEmpty(applyInfo) ?
                String.format(Constants.APPLY_INFO_TEMPLATE, tokenUserInfoDto.getNickName())
                : applyInfo;
        long curTime = System.currentTimeMillis();
        Integer joinType = null;
        String receiveUserId = contactId;
        //查询对方是否已经将你拉黑
        UserContact userContact = userContactMapper.queryOneByUserIdAndContactId(applyUserId, receiveUserId);
        if(userContact != null && ArrayUtils.contains(
                new Integer[]{UserContactStatusEnum.BLACKLIST_BE.getStatus(),
                        UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus()
                },
                userContact.getStatus())
        ){
            throw new BusinessException("不好意思，对方将你这个吊毛拉黑了");
        }
        switch (contactTypeEnum){
            case USER -> {
                UserInfo userInfo = userInfoMapper.selectById(contactId);
                if(userInfo == null){//禁用状态还是可以收到，只是无法操作，等到恢复就行
                    throw new BusinessException("用户不存在。sp");
                }
                joinType = userInfo.getJoinType();
            }
            case GROUP -> {
                GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
                if(groupInfo == null || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())){
                    throw new BusinessException("群聊不存在或者已经解散，吊毛");
                }
                //群主收
                receiveUserId = groupInfo.getGroupOwnerId();
                joinType = groupInfo.getJoinType();
            }
        }
        //直接加入不用记录
        if(JoinTypeEnum.JOIN.getStatus().equals(joinType)){

           addContact(applyUserId,receiveUserId,contactId,contactTypeEnum.getType(),applyInfo);
            return joinType;
        }
        //检查是否申请过
        UserContactApply dbApply = userContactApplyMapper.queryOneByApplyUserIdAndContactId(applyUserId, receiveUserId);
        if(dbApply == null){
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setContactType(contactTypeEnum.getType());
            userContactApply.setReceiveUserId(receiveUserId);
            userContactApply.setApplyUserId(applyUserId);
            userContactApply.setLastApplyTime(curTime);
            userContactApply.setContactId(contactId);
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            this.userContactApplyMapper.insert(userContactApply);
        }else {
            //更新状态
            dbApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            dbApply.setApplyInfo(applyInfo);
            dbApply.setLastApplyTime(curTime);
            this.userContactApplyMapper.updateById(dbApply);
        }
        if(dbApply == null || !UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())){
            //TODO 发送ws消息
        }
        return joinType;
    }

    @Override
    public List<UserContact> loadContact(String userId, String contactType) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.geByName(contactType);
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactType(contactTypeEnum.getType());
        //还有状态 135
        userContact.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus()
        });
        List<UserContact> userContacts = null;
        switch (contactTypeEnum){
            case USER -> {
                //contactUserInfo TRUE 返回联系人名字
                userContact.setContactUserInfo(true);
                userContacts = userContactMapper.loadContact(userContact);
            }
            case GROUP -> {
                //queryGroupInfo TRUE 同时返回群主信息 群的名字
                userContact.setQueryGroupInfo(true);
                userContact.setExcludeMyGroup(true);
                userContacts = userContactMapper.loadContact(userContact);
            }
        }
        return userContacts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum userContactStatusEnum) {
        //移除好友
        LambdaUpdateWrapper<UserContact> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserContact::getUserId,userId).eq(UserContact::getContactId,contactId);
        UserContact userContact = new UserContact();
        userContact.setStatus(userContactStatusEnum.getStatus());
        LocalDateTime now = LocalDateTime.now();
        userContact.setLastUpdateTime(now);
        userContactMapper.update(userContact,wrapper);
        //好友移除自己
        UserContact myFriend = new UserContact();
        myFriend.setLastUpdateTime(now);
        if(UserContactStatusEnum.DEL == userContactStatusEnum){
            myFriend.setStatus(UserContactStatusEnum.DEL_BE.getStatus());
        }
        if(UserContactStatusEnum.BLACKLIST == userContactStatusEnum){
            myFriend.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
        }
        LambdaUpdateWrapper<UserContact> wrapper2 = new LambdaUpdateWrapper<>();
        wrapper2.eq(UserContact::getUserId,contactId).eq(UserContact::getContactId,userId);
        userContactMapper.update(myFriend,wrapper2);
        //TODO 从我的好友列表缓存中删除好友
        //TODO 从好友列表缓存中删除我
    }
    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        //申请加入群聊
        if(UserContactTypeEnum.GROUP.getType().equals(contactType)){
            //群是否满员
            LambdaQueryWrapper<UserContact> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserContact::getContactId,contactId).eq(UserContact::getStatus,UserContactStatusEnum.FRIEND.getStatus());
            int count = userContactMapper.selectCount(wrapper).intValue();
            SysSettingDto sysSetting = redisComponent.getSysSetting();
            if(count >= sysSetting.getMaxGroupCount()){
                throw new BusinessException("群人数已满，无法加入");
            }
        }
        LocalDateTime now = LocalDateTime.now();
        //同意，双方加为好友
//        List<UserContact> userContacts = new Vector<>();
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setCreateTime(now);
        userContact.setLastUpdateTime(now);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        this.saveOrUpdate(userContact,userContact.getUserId(),userContact.getContactId());
       //底层save只会查询主键id，联合主键是能update用，所以只能自己手写
//        userContactService.saveOrUpdate(userContact,wrapper);
//        userContacts.add(userContact);
        //群组不用，user要用
        if(UserContactTypeEnum.USER.getType().equals(contactType)){
            UserContact userContact2 = new UserContact();
            userContact2.setUserId(receiveUserId);
            userContact2.setContactId(applyUserId);
            userContact2.setContactType(contactType);
            userContact2.setCreateTime(now);
            userContact2.setLastUpdateTime(now);
            userContact2.setStatus(UserContactStatusEnum.FRIEND.getStatus());
//            userContacts.add(userContact2);
            this.saveOrUpdate(userContact2,userContact2.getUserId(),userContact2.getContactId());
//            userContactService.saveOrUpdate(userContact2,wrapper);
//
        }
        //批量插入 联合主键存在问题，所以不能使用
//        userContactService.saveOrUpdateBatch(userContacts);
        //TODO 如果是好友，接收人也添加申请人为好友，添加缓存
        //TODO 创建会话
    }
    private void saveOrUpdate(UserContact userContact, String userId,String contactId){
        //查询有没有，有就更新，没有就查询
        UserContact contact = userContactMapper.queryOneByUserIdAndContactId(userId, contactId);
        if(null != contact){
            LambdaUpdateWrapper<UserContact> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(UserContact::getUserId,userContact.getUserId()).eq(UserContact::getContactId,userContact.getContactId());
            userContactMapper.update(userContact,wrapper);
        }else {
            userContactMapper.insert(userContact);
        }

    }

}




