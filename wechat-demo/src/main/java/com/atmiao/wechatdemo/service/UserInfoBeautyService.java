package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.pojo.UserInfoBeauty;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author musichao
* @description 针对表【user_info_beauty】的数据库操作Service
* @createDate 2024-06-05 21:01:13
*/
public interface UserInfoBeautyService extends IService<UserInfoBeauty> {

    Page<UserInfoBeauty> loadBeauty();

    void saveBeautyAccount(UserInfoBeauty userInfoBeauty);
}
