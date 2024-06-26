package com.atmiao.wechatdemo.handlers;


import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.BindException;

/**
 * @author miao
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
@CrossOrigin
public class AGlobalExceptionHandlerController {
    @ExceptionHandler(Exception.class)
    Object handlerException(Exception e, HttpServletRequest request){
        log.info("请求错误，请求地址{}，错误信息；{e}",request.getRequestURL(),e);
        ResponseVo ajaxResponse = new ResponseVo();
        if(e instanceof NoHandlerFoundException){
            ajaxResponse.setCode(ResponseStatusCode.CODE_404.getCode());
            ajaxResponse.setInfo(ResponseStatusCode.CODE_404.getStatus());
            ajaxResponse.setStatus(ResponseStatusCode.CODE_404.getStatus());
        }else if(e instanceof BusinessException){
            //业务错误
            BusinessException biz = (BusinessException) e;
            Integer code = ResponseStatusCode.STATUS_BUSINESS_ERROR.getCode();
            String msg =biz.getMsg();
            String status = ResponseStatusCode.STATUS_BUSINESS_ERROR.getStatus();
            ResponseStatusCode responseStatusCode = biz.getResponseStatusCode();
            if(responseStatusCode != null){
                code = responseStatusCode.getCode();
                msg = responseStatusCode.getStatus();
                status = responseStatusCode.getStatus();
            }
            ajaxResponse.setCode(code);
            ajaxResponse.setInfo(msg);
            ajaxResponse.setStatus(status);
        }else if(e instanceof BindException || e instanceof MethodArgumentTypeMismatchException){
            //参数类型错误
            ajaxResponse.setCode(ResponseStatusCode.STATUS_BUSINESS_ERROR.getCode());
            ajaxResponse.setInfo(ResponseStatusCode.STATUS_BUSINESS_ERROR.getStatus());
            ajaxResponse.setStatus(ResponseStatusCode.STATUS_BUSINESS_ERROR.getStatus());
        }else if(e instanceof DuplicateKeyException){
            ajaxResponse.setCode(ResponseStatusCode.CODE_601.getCode());
            ajaxResponse.setInfo(ResponseStatusCode.CODE_601.getStatus());
            ajaxResponse.setStatus(ResponseStatusCode.CODE_601.getStatus());
        } else {
            ajaxResponse.setCode(ResponseStatusCode.STATUS_SERVER_ERROR.getCode());
            ajaxResponse.setInfo(ResponseStatusCode.STATUS_SERVER_ERROR.getStatus());
            ajaxResponse.setStatus(ResponseStatusCode.STATUS_SERVER_ERROR.getStatus());
        }
        return ajaxResponse;
    }
}
