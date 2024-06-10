package com.atmiao.wechatdemo.commons;

import lombok.Data;

/**
 * @author miao
 * @version 1.0
 */

public enum ResponseStatusCode {
    CODE_404(404,"PAGE NOT FOUND"),
    CODE_601(601,"primary key collide"),
    STATUS_SUCCESS(200,"success"),
    STATUS_BUSINESS_ERROR(600,"business error"),
    STATUS_SERVER_ERROR(500,"server error");

    private Integer code;
    private  String status;

    private ResponseStatusCode(Integer code, String status) {
        this.code = code;
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }
}
