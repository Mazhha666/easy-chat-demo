package com.atmiao.wechatdemo.service.impl;

import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.enums.PageSize;
import com.atmiao.wechatdemo.commons.enums.UserContactApplyStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.mapper.UserContactMapper;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.service.UserContactService;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.UserContactApply;
import com.atmiao.wechatdemo.service.UserContactApplyService;
import com.atmiao.wechatdemo.mapper.UserContactApplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
* @author musichao
* @description 针对表【user_contact_apply】的数据库操作Service实现
* @createDate 2024-06-12 18:28:24
*/
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
    implements UserContactApplyService{
    @Autowired
    private UserContactApplyMapper userContactApplyMapper;
    @Autowired
    private UserContactMapper userContactMapper;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserContactService userContactService;
    @Override
    public Page<UserContactApply> loadApply(TokenUserInfoDto tokenUserInfoDto, Integer pageNo) {

        Page<UserContactApply> applyPage = new Page<>(pageNo, PageSize.SIZE15.getSize());
//        LambdaQueryWrapper<UserContactApply> wrapper = new LambdaQueryWrapper<>();
//        //自己调用这个方法
//        wrapper.eq(UserContactApply::getReceiveUserId,tokenUserInfoDto.getUserId())
//                .orderByDesc(UserContactApply::getLastApplyTime);
//        Page<UserContactApply> pages = userContactApplyMapper.selectPage(applyPage, wrapper);
        Page<UserContactApply> pages = userContactApplyMapper.loadApply(applyPage, tokenUserInfoDto.getUserId(), true);
        return pages;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delWith(String userId, Integer applyId, Integer status) {
        UserContactApplyStatusEnum applyStatusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if(null == applyStatusEnum || UserContactApplyStatusEnum.INIT == applyStatusEnum){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        //校验
        UserContactApply userContactApply = userContactApplyMapper.selectById(applyId);
        if(userContactApply == null || !userId.equals(userContactApply.getReceiveUserId())){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        //乐观锁更新数据，status 版本必须为INIT0才能更新，处理并发
        LambdaUpdateWrapper<UserContactApply> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserContactApply::getApplyId,applyId).eq(UserContactApply::getStatus, UserContactApplyStatusEnum.INIT.getStatus());
        userContactApply.setLastApplyTime(System.currentTimeMillis());
        userContactApply.setStatus(status);
        int rows = userContactApplyMapper.update(userContactApply, wrapper);
        if(rows == 0){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if(UserContactApplyStatusEnum.PASS == applyStatusEnum){

            userContactService.addContact(userContactApply.getApplyUserId(),userContactApply.getReceiveUserId(),userContactApply.getContactId(), userContactApply.getContactType(), userContactApply.getApplyInfo());
            return;
        }
        if(UserContactApplyStatusEnum.BLACKLIST == applyStatusEnum){
            LocalDateTime now = LocalDateTime.now();
            //更新或者加入信息
            UserContact userContact = new UserContact();
            userContact.setUserId(userContactApply.getApplyUserId());
            userContact.setLastUpdateTime(now);
            //第一次被拉黑
            userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
            UserContact userContact1 = userContactMapper.selectById(userContact.getUserId());
            if(userContact1 != null){
                userContactMapper.updateById(userContact);

            } else {
                userContact.setContactId(userContactApply.getContactId());
                userContact.setContactType(userContactApply.getContactType());
                userContact.setCreateTime(now);
                userContactMapper.insert(userContact);
            }
        }
    }


}




