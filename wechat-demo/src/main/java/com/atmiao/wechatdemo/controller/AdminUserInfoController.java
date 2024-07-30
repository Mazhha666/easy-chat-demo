package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author miao
 * @version 1.0
 */
@RestController("adminUserInfoController")
@CrossOrigin
@Slf4j
@Tag(name = "管理员信息模块",description = "管理员信息等接口")
@RequestMapping("admin")
@Validated
public class AdminUserInfoController {
    @Autowired
    private UserInfoService userInfoService;
    @Operation(summary = "loadUser",description = "得到全部用户信息")
    @PostMapping("loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo loadUser(){
        //{current: 1, size: 20, userId: 'dasdas', nickNameFuzzy: 'dasdas'}
        Page<UserInfo> page = userInfoService.loadUser();
        return ResponseVo.getSuccessResponseVo(page);

    }
    @Operation(summary = "updateUserStatus",description = "禁用启用用户")
    @PostMapping("updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo updateUserStatus(@RequestParam("status")@NotNull Integer status,@NotEmpty String userId){
        userInfoService.updateUserStatus(status,userId);
        return ResponseVo.getSuccessResponseVo(null);

    }
    @Operation(summary = "forceOffLine",description = "强制下线")
    @PostMapping("forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo forceOffLine(@RequestParam("userId")@NotEmpty String userId){
        userInfoService.forceOffline(userId);
        return ResponseVo.getSuccessResponseVo(null);

    }
}
