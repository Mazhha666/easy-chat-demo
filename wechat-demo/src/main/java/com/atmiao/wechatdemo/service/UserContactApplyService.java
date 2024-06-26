package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.pojo.UserContactApply;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author musichao
* @description 针对表【user_contact_apply】的数据库操作Service
* @createDate 2024-06-12 18:28:24
*/
public interface UserContactApplyService extends IService<UserContactApply> {

    Page<UserContactApply> loadApply(TokenUserInfoDto tokenUserInfoDto, Integer pageNo);

    void delWith(String userId, Integer applyId, Integer status);

}
