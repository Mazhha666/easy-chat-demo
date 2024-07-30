package com.atmiao.wechatdemo.service.impl;

import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.enums.BeautyAccountStatusEnum;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.mapper.UserInfoMapper;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.UserInfoBeauty;
import com.atmiao.wechatdemo.service.UserInfoBeautyService;
import com.atmiao.wechatdemo.mapper.UserInfoBeautyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author musichao
* @description 针对表【user_info_beauty】的数据库操作Service实现
* @createDate 2024-06-05 21:01:13
*/
@Service
public class UserInfoBeautyServiceImpl extends ServiceImpl<UserInfoBeautyMapper, UserInfoBeauty>
    implements UserInfoBeautyService{
    @Autowired
    private UserInfoBeautyMapper userInfoBeautyMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public Page<UserInfoBeauty> loadBeauty() {
        //TODO 后面再修改 分页啥的
        Page<UserInfoBeauty> page = new Page<>(1,20);
        LambdaQueryWrapper<UserInfoBeauty> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserInfoBeauty::getId);
        Page<UserInfoBeauty> userInfoBeautyPage = userInfoBeautyMapper.selectPage(page, wrapper);
        return userInfoBeautyPage;
    }

    @Override
    public void saveBeautyAccount(UserInfoBeauty userInfoBeauty) {
        if(userInfoBeauty.getId() != null){
            //修改
            UserInfoBeauty dbBeauty = userInfoBeautyMapper.selectById(userInfoBeauty.getId());
            if(BeautyAccountStatusEnum.USED.getStatus().equals(dbBeauty.getStatus())){
                throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
            }
        }
        UserInfoBeauty dbInfo = userInfoBeautyMapper.queryOneByEmail(userInfoBeauty.getEmail());
       //新增
        if(userInfoBeauty.getId() == null && dbInfo != null){
            //邮箱已经注册 邮箱靓号唯一
            throw new BusinessException("靓号邮箱已经被注册，请换一个，吊毛");
        }
        //修改
        if(userInfoBeauty.getId() != null && dbInfo != null &&
                dbInfo.getId() != null &&
                !userInfoBeauty.getId().equals(dbInfo.getId())){
            throw new BusinessException("靓号邮箱已经被注册，请换一个，吊毛");
        }
        //判断靓号是否存在
        dbInfo = userInfoBeautyMapper.queryOneByUserId(userInfoBeauty.getUserId());
        if(userInfoBeauty.getId() == null && dbInfo != null){
            //邮箱已经注册 邮箱靓号唯一
            throw new BusinessException("靓号已经存在");
        }
        //修改
        if(userInfoBeauty.getId() != null && dbInfo != null &&
                dbInfo.getId() != null &&
                !userInfoBeauty.getId().equals(dbInfo.getId())){
            throw new BusinessException("靓号已经被使用");
        }
        //判断是否再用户信息表存在此邮箱
        UserInfo userInfo = userInfoMapper.queryOneByEmail(userInfoBeauty.getEmail());
        if(null != userInfo){
            throw new BusinessException("靓号邮箱已经被注册");
        }
        userInfo = userInfoMapper.queryOneByUserId(userInfoBeauty.getUserId());
        if(null != userInfo){
            throw  new BusinessException("靓号已经被注册");
        }
        //更新或插入
        if(userInfoBeauty.getId() != null){
            userInfoBeautyMapper.updateById(userInfoBeauty);
        }else {
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());
            userInfoBeautyMapper.insert(userInfoBeauty);
        }
    }
}




