package com.atmiao.wechatdemo.utils;

import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * @author miao
 * @version 1.0
 */
public class CommonUtils {
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
