package com.atmiao.wechatdemo.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.enums.GroupStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.mapper.UserContactMapper;
import com.atmiao.wechatdemo.pojo.GroupMFVo;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.service.GroupInfoService;
import com.atmiao.wechatdemo.mapper.GroupInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
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
            //TODO 创建会话
            //TODO 发送消息
        } else {
            //校验token所得的userid所拥有的群组与所传入groupId是否包含，避免传入其他人的groupId
            GroupInfo groupInfo1 = groupInfoMapper.selectById(groupInfo.getGroupId());
            if (!groupInfo1.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
            }
            this.groupInfoMapper.updateById(groupInfo);
            //TODO 更新相关表冗余信息
            //TODO 修改群名称发送ws消息
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
        //TODO 解散的不查询还是保留 .eq(GroupInfo::getStatus,GroupStatusEnum.NOMAL.getStatus())
        wrapper.eq(GroupInfo::getGroupOwnerId,userId).orderByDesc(GroupInfo::getCreateTime);
        List<GroupInfo> groupInfoList = groupInfoMapper.selectList(wrapper);
        return groupInfoList;
    }

    @Override
    public Page<GroupInfo> loadGroup() {
        //TODO 后续修改size 不然数量还是0
        Page<GroupInfo> page = new Page<>(0, -1);
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
        //TODO 移除相关群员的联系人缓存
        //TODO 1.更新会话信息 2.记录群消息 3.发送消息

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




