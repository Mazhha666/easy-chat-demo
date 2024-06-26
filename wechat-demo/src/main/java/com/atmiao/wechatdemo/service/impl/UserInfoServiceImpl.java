package com.atmiao.wechatdemo.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.enums.BeautyAccountStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import com.atmiao.wechatdemo.commons.enums.UserStatusEnum;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.mapper.UserInfoBeautyMapper;
import com.atmiao.wechatdemo.pojo.RegisterPojo;
import com.atmiao.wechatdemo.pojo.TokenUserInfoVo;
import com.atmiao.wechatdemo.pojo.UserInfoBeauty;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.JwtHelper;
import com.atmiao.wechatdemo.utils.MD5Util;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.atmiao.wechatdemo.mapper.UserInfoMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Objects;

/**
* @author musichao
* @description 针对表【user_info】的数据库操作Service实现
* @createDate 2024-06-05 21:00:20
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserInfoBeautyMapper userInfoBeautyMapper;
    @Autowired
    AppConfig appConfig;
    @Autowired
    RedisComponent redisComponent;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(RegisterPojo registerPojo) {
        //TODO 先去数据库查询邮箱是否存在，满足唯一性
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getEmail, registerPojo.getEmail());
        UserInfo userInfo = userInfoMapper.selectOne(wrapper);
        if (null != userInfo) {
            throw new BusinessException("邮箱已经被使用");
        }
        // 普通userId
        String userId = CommonUtils.getUserId();
        // 靓号的赋予(此邮箱是靓号而且未被使用)
        LambdaQueryWrapper<UserInfoBeauty> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(UserInfoBeauty::getEmail, registerPojo.getEmail());
        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectOne(wrapper2);
        boolean userInfoBeautyFlag = userInfoBeauty != null && userInfoBeauty.getStatus() == BeautyAccountStatusEnum.NO_USE.getStatus();
        if (userInfoBeautyFlag) {
            //设置靓号的id
            userId = UserContactTypeEnum.USER.getPrefix() + userInfoBeauty.getUserId();
        }
        // 插入数据
        LocalDateTime now = LocalDateTime.now();
        Date date = new Date(now.toInstant(ZoneOffset.ofHours(+8)).toEpochMilli());
        UserInfo newUser = new UserInfo();
        newUser.setUserId(userId);
        newUser.setEmail(registerPojo.getEmail());
        newUser.setNickName(registerPojo.getNickName());
        newUser.setJoinType(0);
        newUser.setSex(0);
        newUser.setPassword(MD5Util.encrypt(registerPojo.getPassword()));
        newUser.setStatus(UserStatusEnum.ENABLE.getStatus());
        newUser.setCreateTime(now);
        newUser.setLastOffTime(date.getTime());
        userInfoMapper.insert(newUser);
        // 数据库靓号的改变
        if (userInfoBeautyFlag) {
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
            userInfoBeautyMapper.updateById(userInfoBeauty);
        }
        //TODO 创建机器人好友(接入chatgpt)

    }

    @Override
    public TokenUserInfoVo login(RegisterPojo registerPojo) {
        // 先去数据库查询邮箱是否存在，满足唯一性
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getEmail, registerPojo.getEmail());
        UserInfo userInfo = userInfoMapper.selectOne(wrapper);
        if (null == userInfo || !userInfo.getPassword().equals(MD5Util.encrypt(registerPojo.getPassword())))
            throw new BusinessException("密码错误");
        // 查看是否封禁
        if(Objects.equals(userInfo.getStatus(), UserStatusEnum.DISABLE.getStatus())){
            throw  new BusinessException("用户被封禁");
        }
        //token的返回 admin权限的检测
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);
        // 查询我的群组，
        // 查询我的好友
        //检查心跳，验证是否重复登录
        Long userHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if(userHeartBeat != null){
            throw new BusinessException("用户已经登录，请退出重新登录");
        }
        Long tokenId = Long.valueOf(userInfo.getUserId().substring(1));
        //获得token
        String token = JwtHelper.createToken(tokenId);
        tokenUserInfoDto.setToken(token);
        //redis保存token
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
        //封装返回TokenUserInfoVo
        //封装对象，返回token，admin权限等
        TokenUserInfoVo tokenUserInfoVo = new TokenUserInfoVo();
        tokenUserInfoVo.copyFromUserInfo(userInfo);
        tokenUserInfoVo.setToken(tokenUserInfoDto.getToken());
        tokenUserInfoVo.setAdmin(tokenUserInfoDto.getAdmin());
        return  tokenUserInfoVo ;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) {
        if(null != avatarFile){
            //保存头像到本地
            String baseFolder = appConfig.getProjectFolder()  + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
            if(!targetFileFolder.exists()){
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userInfo.getUserId() + Constants.IMAGE_SUFFIX;
            try {
                avatarFile.transferTo(new File(filePath));
                avatarFile.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        UserInfo dbInfo = userInfoMapper.selectById(userInfo.getUserId());
        userInfoMapper.updateById(userInfo);
        String contactNameUpdate = null;
        if(!dbInfo.getNickName().equals(userInfo.getNickName())){
            contactNameUpdate = userInfo.getNickName();
        }
        //TODO 更新会话中的昵称信息
    }

    @Override
    public Page<UserInfo> loadUser() {
        //TODO 后面再修改 分页啥的
        Page<UserInfo> userInfoPage = new Page<>(0,-1);
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserInfo::getJoinType);
        Page<UserInfo> infoPage = userInfoMapper.selectPage(userInfoPage, wrapper);
        return infoPage;
    }

    @Override
    public void updateUserStatus(Integer status, String userId) {
        UserStatusEnum userEnum = UserStatusEnum.getByStatus(status);
        if(userEnum == null){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setStatus(userEnum.getStatus());
        userInfoMapper.updateById(userInfo);

    }

    @Override
    public void forceOffline(String userId) {
        //TODO 强制下线
    }

    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo){
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(userInfo.getNickName());
        String adminEmails = appConfig.getAdminEmails();
        //检测满足properties的满足的admin邮箱权限(super用户)
        if(!StringUtils.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","),userInfo.getEmail())){
            tokenUserInfoDto.setAdmin(true);
        }else {
            tokenUserInfoDto.setAdmin(false);
        }
        return tokenUserInfoDto;
    }

}




