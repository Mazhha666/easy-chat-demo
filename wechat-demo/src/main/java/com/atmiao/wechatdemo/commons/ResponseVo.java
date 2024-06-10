package com.atmiao.wechatdemo.commons;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

/**
 * @author miao
 * @version 1.0
 */
@Data
public class ResponseVo<T> {
    private String status;
    private Integer code;
    private String info;
    private  T data;
    public static<T> ResponseVo<T> getResponseVo(T t,ResponseStatusCode statusCode){
        ResponseVo<T> response = new ResponseVo<>();
        response.setStatus(statusCode.getStatus());
        response.setCode(statusCode.getCode());
        response.setData(t);
        response.setInfo(statusCode.getStatus());
        return  response;
    }
    public static<T> ResponseVo<T> getSuccessResponseVo(T t){
        return getResponseVo(t,ResponseStatusCode.STATUS_SUCCESS);
    }
    public static<T> ResponseVo<T> getBussinessErrorResponseVo(T t){
        return getResponseVo(t,ResponseStatusCode.STATUS_BUSINESS_ERROR);
    }
    public static<T> ResponseVo<T> getServerErrorResponseVo(T t){
        return getResponseVo(t,ResponseStatusCode.STATUS_SERVER_ERROR);
    }
}
