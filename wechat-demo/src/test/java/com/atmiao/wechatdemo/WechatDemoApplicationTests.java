package com.atmiao.wechatdemo;

import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.controller.AdminAppUpdateController;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.mapper.UserInfoMapper;
import com.atmiao.wechatdemo.pojo.AppUpdate;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.service.AppUpdateService;
import com.atmiao.wechatdemo.service.GroupInfoService;
import com.atmiao.wechatdemo.service.UserContactService;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.JwtHelper;
import com.atmiao.wechatdemo.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

@SpringBootTest
class WechatDemoApplicationTests {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    AppConfig appConfig;
    @Autowired
    UserContactService userContactService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    GroupInfoService groupInfoService;
    @Autowired
    AppUpdateService appUpdateService;

    @Test
    void contextLoads() {
        String userId = CommonUtils.getUserId();
        System.out.println(userId);
        System.out.println(Long.valueOf(userId.substring(1)));
        String token = JwtHelper.createToken(Long.valueOf(userId.substring(1)));
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
    }
    @Test
    void myBatis(){
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getEmail,"test@qq.com");
        UserInfo userInfo = userInfoMapper.selectOne(wrapper);
        System.out.println(userInfo);
    }
    @Test
    void redisTest(){
        String token = "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAA_6tWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNDe0MDI3NzIy1VEqLU4t8kxRsjI2MDc2MTE1BdK1AABmgeU5AAAA.n0vZ8jSBVtJ0DhzzKgi8oJhtKe2yNemcsUjsN4Jy1Tdv2Mcn-N_-grU2dFpQ5jonStauqcO40k4_xmkNQTXDAA";
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        System.out.println(tokenUserInfoDto.getUserId() + tokenUserInfoDto.getNickName() + tokenUserInfoDto.getAdmin());
    }
    @Test
    void pathtTest(){
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if(!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + "g12345678999" + Constants.IMAGE_SUFFIX;
        System.out.println(filePath);
    }
    @Test
    void loadContact(){
        List<UserContact> userContactList = userContactService.loadContact("U38891588356", "group");
        System.out.println(userContactList);
    }
    @Test
    void loadUser(){
        Page<UserInfo> userInfoPage = userInfoService.loadUser();
        System.out.println(userInfoPage.getRecords());
        System.out.println(userInfoPage.getPages());
        System.out.println(userInfoPage.getTotal());
        System.out.println(userInfoPage.getSize());

    }
    @Test
    void loadGroup(){
        Page<GroupInfo> groupInfoPage = groupInfoService.loadGroup();
        System.out.println(groupInfoPage.getRecords());
        System.out.println(groupInfoPage.getTotal());

    }
    @Test
    void updateAPP(){
        Page<AppUpdate> appUpdatePage = appUpdateService.loadUpdateList();
        System.out.println(appUpdatePage.getRecords());

    }

}
