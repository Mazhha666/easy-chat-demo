package com.atmiao.wechatdemo.exception;

import com.atmiao.wechatdemo.commons.ResponseStatusCode;

/**
 * @author miao
 * @version 1.0
 */

public class BusinessException extends RuntimeException {
    private String msg;
    private ResponseStatusCode responseStatusCode;
    public BusinessException(String message) {
        super(message);
        this.msg = message;
    }
    public BusinessException(ResponseStatusCode responseStatusCode) {
        super();
        this.responseStatusCode = responseStatusCode;
    }

    public ResponseStatusCode getResponseStatusCode() {
        return responseStatusCode;
    }

    public String getMsg() {
        return msg;
    }
}
