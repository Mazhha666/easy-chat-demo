package com.atmiao.wechatdemo.commons;

import com.atmiao.wechatdemo.commons.enums.UserContactTypeEnum;

/**
 * @author miao
 * @version 1.0
 */
public class Constants {
    public static final String REDIS_KEY_CHECK_CODE = "easychat:checkcode:";
    public static final Integer REDIS_TIME_1MIN = 60;
    public static final Integer REDIS_KEY_EXPIRE_DAY = REDIS_TIME_1MIN * 24 * 60;
    public static final Integer Length_11 = 11;
    public static final String REDIS_KEY_USER_HEART_BEAT = "easychat:ws:user:heartbeat";
    public static final String REDIS_KEY_WS_TOKEN= "easychat:ws:token:";
    public static final String REDIS_KEY_WS_TOKEN_USERID= "easychat:ws:token:userid";
    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";
    public static final String REDIS_KEY_SYS_SETTING = "easychat:syssetting:";
    public static final String FILE_FOLDER_FILE = "/file/";
    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";
    public static final String IMAGE_SUFFIX = ".png";
    public static final String  COVER_IMAGE_SUFFIX = "_cover.png";
    public static final String  APPLY_INFO_TEMPLATE = "hello 我是%s";
    public static final String  REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,10}$";
    public static final String  APP_UPDATE_FOLDER = "/app/";
    public static final String  APP_EXE_SUFFIX = ".exe";
    public static final String  APP_NAME = "EasyChatSetUp";


}
