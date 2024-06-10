package com.atmiao.wechatdemo.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author miao
 * @version 1.0
 */
@Data
public class RegisterPojo {
    @NotEmpty
    private String checkCodeKey;
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    private String password;
    @NotEmpty
    private String nickName;
    @NotEmpty
    private String checkCode;
}
