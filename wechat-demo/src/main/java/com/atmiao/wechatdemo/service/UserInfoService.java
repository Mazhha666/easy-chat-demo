package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.pojo.RegisterPojo;
import com.atmiao.wechatdemo.pojo.TokenUserInfoVo;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author musichao
* @description 针对表【user_info】的数据库操作Service
* @createDate 2024-06-05 21:00:20
*/
public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterPojo registerPojo);

    TokenUserInfoVo login(RegisterPojo registerPojo);

    void saveUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover);

    Page<UserInfo> loadUser();

    void updateUserStatus(Integer status, String userId);

    void forceOffline(String userId);
}
