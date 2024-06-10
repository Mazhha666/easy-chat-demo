package com.atmiao.wechatdemo.commons;

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
}
