package com.atmiao.wechatdemo.exception;

/**
 * @author miao
 * @version 1.0
 */

public class BusinessException extends RuntimeException {
    private String msg;
    public BusinessException(String message) {
        super(message);
        this.msg = message;
    }

    public String getMsg() {
        return msg;
    }
}
