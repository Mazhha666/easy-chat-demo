package com.atmiao.wechatdemo.utils;

import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author miao
 * @version 1.0
 */
@Component("redisComponent")
public class RedisComponent {
    @Autowired
    private RedisUtils redisUtils;
    public Long getUserHeartBeat(String userId){
        return (Long) redisUtils.get(Constants.REDIS_KEY_USER_HEART_BEAT + userId);
    }
    public void saveTokenUserInfoDto(TokenUserInfoDto userInfoDto){
        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN + userInfoDto.getToken(),userInfoDto,Constants.REDIS_KEY_EXPIRE_DAY *2);
        //TODO 可能存在问题
        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN_USERID + userInfoDto.getUserId(),userInfoDto.getToken(),Constants.REDIS_KEY_EXPIRE_DAY *2);
    }
}
