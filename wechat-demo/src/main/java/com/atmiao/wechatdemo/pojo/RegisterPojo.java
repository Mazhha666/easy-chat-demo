package com.atmiao.wechatdemo.pojo;

import com.atmiao.wechatdemo.commons.Constants;
import jakarta.validation.constraints.*;
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
    @Pattern(regexp = Constants.REGEX_PASSWORD)
    private String password;
    @NotEmpty
    private String nickName;
    @NotEmpty
    private String checkCode;
}
