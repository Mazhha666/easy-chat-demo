package com.atmiao.wechatdemo;

import com.atmiao.wechatdemo.utils.CommonUtils;
import com.atmiao.wechatdemo.utils.JwtHelper;

/**
 * @author miao
 * @version 1.0
 */
public class NormalTest {
    public static void main(String[] args) {
        String userId = CommonUtils.getUserId();
        System.out.println(userId);
       Long id = Long.valueOf(userId.substring(1));
        System.out.println(id);
        String token = JwtHelper.createToken(id);
        System.out.println(token);
        String string = JwtHelper.getUserId(token).toString();
        System.out.println(string);
    }
}
