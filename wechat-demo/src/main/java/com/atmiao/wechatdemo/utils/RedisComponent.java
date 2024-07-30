package com.atmiao.wechatdemo.utils;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.pojo.UserContact;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author miao
 * @version 1.0
 */
@Component("redisComponent")
public class RedisComponent {
    @Autowired
    private RedisUtils redisUtils;

    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(Constants.REDIS_KEY_USER_HEART_BEAT + userId);
    }

    public void saveUserHeartBeat(String userId) {
        redisUtils.set(Constants.REDIS_KEY_USER_HEART_BEAT + userId, System.currentTimeMillis(), Constants.REDIS_KEY_EXPIRE_HEART_BEAT);
    }

    public void saveTokenUserInfoDto(TokenUserInfoDto userInfoDto) {
        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN + userInfoDto.getToken(), userInfoDto, Constants.REDIS_KEY_EXPIRE_DAY * 2);
        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN_USERID + userInfoDto.getUserId(), userInfoDto.getToken(), Constants.REDIS_KEY_EXPIRE_DAY * 2);
    }

    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        return sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
    }

    public TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest request) {
        String token = request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        return tokenUserInfoDto;
    }

    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        return tokenUserInfoDto;
    }

    public void saveSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    //清空联系人
    public void cleanUserContact(String userId) {
        redisUtils.del(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

    //批量添加联系人
    public void addUserContactBatch(String userId, List<String> contactList) {
        //需要遍历加入
        redisUtils.listPush(Constants.REDIS_KEY_USER_CONTACT + userId,contactList,Constants.REDIS_KEY_EXPIRE_DAY_TIME_UNIT);
    }

    public List<String> getUserContactList(String userId) {
        List<Object> objects = redisUtils.lGet(Constants.REDIS_KEY_USER_CONTACT + userId, 0, -1);
        return objects.stream().map(item -> item.toString()).collect(Collectors.toList());
    }
    public void cleanUserTokenByUserId(String userId){
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        if(StringUtils.isEmpty(token)){
            return;
        }
        redisUtils.del(Constants.REDIS_KEY_WS_TOKEN + token);
    }
    public void removeUserHeartBeat(String userId) {
        redisUtils.del(Constants.REDIS_KEY_USER_HEART_BEAT + userId);
    }

    public void addUserContact(String userId,String contactId) {
        List<String> userContactList = getUserContactList(contactId);
        if(userContactList.contains(contactId)){
            return;
        }
        redisUtils.lSet(Constants.REDIS_KEY_USER_CONTACT + userId,contactId,Constants.REDIS_KEY_EXPIRE_DAY * 2);
    }

    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        return getTokenUserInfoDto(token);
    }


    public void removeUserContact(String userId, String contactId) {
        //0全部移除
        redisUtils.lRemove(Constants.REDIS_KEY_USER_CONTACT + userId,0,contactId);
    }
}
