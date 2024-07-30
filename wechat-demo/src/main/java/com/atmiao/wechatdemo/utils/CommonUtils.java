package com.atmiao.wechatdemo.utils;

import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Random;

/**
 * @author miao
 * @version 1.0
 */
public class CommonUtils {
    public static boolean isNumber(String str){
        String regExp = "^[0-9]+$";
        if(null == str){
            return false;
        }
        if(!str.matches(regExp)){
            return false;
        }
        return true;
    }
    public static String getFileSuffix(String fileName){
        if(StringUtils.isEmpty(fileName)){
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    public static final String getChatSessionId(String[] userIds){
        Arrays.sort(userIds);
        return MD5Util.encrypt(StringUtils.join(userIds,""));

    }
    public static final String getChatSessionId4Group(String groupId){
        return MD5Util.encrypt(groupId);
    }
    public static String cleanHtmlTag(String content){
        if(StringUtils.isEmpty(content)){
            return content;
        }
        content = content.replace("<","&lt");
        content = content.replace("\r\n","<br>");
        content = content.replace("\n","<br>");
        return content;
    }
    public static String getGroupId(){
        return UserContactTypeEnum.GROUP.getPrefix() + getRandomNumber(Constants.Length_11);
    }
    public static String getUserId(){
        return UserContactTypeEnum.USER.getPrefix() + getRandomNumber(Constants.Length_11);
    }
    public static String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }
    public static String getRandomString(Integer count){
        return RandomStringUtils.random(count,true,true);
    }
}
