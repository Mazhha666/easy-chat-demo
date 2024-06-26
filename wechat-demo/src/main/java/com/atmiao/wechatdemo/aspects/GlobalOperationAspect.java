package com.atmiao.wechatdemo.aspects;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.utils.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.function.ServerRequest;

import java.lang.reflect.Method;

/**
 * @author miao
 * @version 1.0
 */
@Component
@Aspect
@Slf4j
public class GlobalOperationAspect {
    @Autowired
    RedisUtils redisUtils;
//    @Pointcut(value = "execution(* com.atmiao.wechatdemo.*.*(..))")
//    public void point(){}
    @Before("@annotation(com.atmiao.wechatdemo.annotions.GlobalInterceptor)")
    public void interceptorDo(JoinPoint joinPoint){
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if(interceptor == null){
                return;
            }
            if(interceptor.checkLogin() || interceptor.checkAdmin()){
                checkLogin(interceptor.checkAdmin());
            }
        }catch (BusinessException e){
            log.error("全局拦截异常",e);
            throw e;
        }catch (Exception e){
            log.error("全局拦截异常",e);
            throw  new BusinessException("拦截业务错误");
        }catch (Throwable e){
            log.error("全局拦截异常",e);
            throw new BusinessException("拦截业务错误");
        }


    }
    private void checkLogin(Boolean checkAdmin){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        if(StringUtils.isEmpty(token)){
            //压根就没登录过
            throw new BusinessException(ResponseStatusCode.CODE_901);
        }
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if(tokenUserInfoDto == null){
            //token失效,登陆超时
            throw new BusinessException(ResponseStatusCode.CODE_901);
        }
        if(checkAdmin && !tokenUserInfoDto.getAdmin()){
            //请求不存在（只能admin权限可以看）
            throw new BusinessException(ResponseStatusCode.CODE_404);
        }
    }
}
