package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.pojo.RegisterPojo;
import com.atmiao.wechatdemo.pojo.TokenUserInfoVo;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author musichao
* @description 针对表【user_info】的数据库操作Service
* @createDate 2024-06-05 21:00:20
*/
public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterPojo registerPojo);

    TokenUserInfoVo login(RegisterPojo registerPojo);
}
