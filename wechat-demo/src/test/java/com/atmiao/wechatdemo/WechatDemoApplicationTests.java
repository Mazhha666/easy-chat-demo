package com.atmiao.wechatdemo;

import com.atmiao.wechatdemo.mapper.UserInfoMapper;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.JwtHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WechatDemoApplicationTests {
    @Autowired
    UserInfoMapper userInfoMapper;

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

}
