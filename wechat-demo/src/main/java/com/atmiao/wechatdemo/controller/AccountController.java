package com.atmiao.wechatdemo.controller;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.pojo.RegisterPojo;
import com.atmiao.wechatdemo.pojo.TokenUserInfoVo;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.atmiao.wechatdemo.utils.RedisUtils;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author miao
 * @version 1.0
 */
@RestController("accountController")
@CrossOrigin
@Slf4j
@Tag(name = "用户登录模块",description = "用户登录验证码等接口")
@RequestMapping("account")
public class AccountController {
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    RedisComponent redisComponent;
    @Operation(summary = "checkCode",description = "生成验证码的方法")
    @PostMapping("/checkCode")
    public ResponseVo<Object> checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        log.info("验证码是{}",code);
        log.info("checkcodeKey:{}",checkCodeKey);
        redisUtils.set(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code,Constants.REDIS_TIME_1MIN * 10);
        String checkCodeBase64 = captcha.toBase64();
        Map<String,String> result = new HashMap<>();
        result.put("checkCode",checkCodeBase64);
        result.put("checkCodeKey",checkCodeKey);
        return ResponseVo.getSuccessResponseVo(result);
    }
    @Operation(summary = "register",description = "注册功能")
    @PostMapping("register")
    public ResponseVo register(@Validated RegisterPojo registerPojo, BindingResult result){

        String res = (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + registerPojo.getCheckCodeKey());
        try {
            if(result.hasErrors()){
                throw new BusinessException("不符合规范");
            }
            if(!StringUtils.equalsIgnoreCase(res,registerPojo.getCheckCode())){
                throw new BusinessException("验证码输入错误");
            }
            userInfoService.register(registerPojo);
            return ResponseVo.getSuccessResponseVo(null);
        } finally {
            //验证不通过就要重新发验证码验证，避免爆破
            redisUtils.del(Constants.REDIS_KEY_CHECK_CODE + registerPojo.getCheckCodeKey());
        }
    }
    @Operation(summary = "login",description = "登录功能")
    @PostMapping("login")
    public ResponseVo login( @Validated RegisterPojo registerPojo, BindingResult result){

        String res = (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + registerPojo.getCheckCodeKey());
        System.out.println(res);
        try {
            if(result.hasErrors()){
                throw new BusinessException("不符合规范");
            }
            if(!StringUtils.equalsIgnoreCase(res,registerPojo.getCheckCode())){
                throw new BusinessException("验证码输入错误");
            }
            TokenUserInfoVo tokenUserInfoVo = userInfoService.login(registerPojo);
            return ResponseVo.getSuccessResponseVo(tokenUserInfoVo);
        } finally {
            //验证不通过就要重新发验证码验证，避免爆破
            redisUtils.del(Constants.REDIS_KEY_CHECK_CODE + registerPojo.getCheckCodeKey());

        }
    }
    @Operation(summary = "getSysSetting",description = "得到系统设置")
    @PostMapping("getSysSetting")
    @GlobalInterceptor
    public ResponseVo getSysSetting(){
        return ResponseVo.getSuccessResponseVo(redisComponent.getSysSetting());
    }
}
