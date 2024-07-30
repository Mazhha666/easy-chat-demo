package com.atmiao.wechatdemo.commons;

import lombok.Data;

/**
 * @author miao
 * @version 1.0
 */

public enum ResponseStatusCode {
    CODE_404(404,"PAGE NOT FOUND"),
    CODE_601(601,"primary key collide"),
    CODE_602(602,"文件不存在"),
    CODE_901(901,"login excess time"),
    CODE_902(902,"你还不是对方的好友，请先向好友发送验证申请"),
    CODE_903(903,"你已经不在群聊，请联系管理员"),

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
