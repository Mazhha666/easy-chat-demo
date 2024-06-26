package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.commons.enums.BeautyAccountStatusEnum;
import com.atmiao.wechatdemo.commons.enums.UserContactApplyStatusEnum;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.pojo.UserInfo;
import com.atmiao.wechatdemo.pojo.UserInfoBeauty;
import com.atmiao.wechatdemo.service.UserInfoBeautyService;
import com.atmiao.wechatdemo.service.UserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author miao
 * @version 1.0
 */
@RestController("adminUserInfoBeautyController")
@CrossOrigin
@Slf4j
@Tag(name = "管理员靓号信息模块",description = "管理员靓号信息等接口")
@RequestMapping("admin")
@Validated
public class AdminUserInfoBeautyController {

    @Autowired
    private UserInfoBeautyService userInfoBeautyService;
    @Operation(summary = "loadBeautyAccountList",description = "得到全部靓号用户信息")
    @GetMapping("loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo loadBeautyAccountList(){
        Page<UserInfoBeauty> page = userInfoBeautyService.loadBeauty();
        return ResponseVo.getSuccessResponseVo(page);

    }
    @Operation(summary = "saveBeautyAccount",description = "保存靓号")
    @GetMapping("saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo saveBeautyAccount(UserInfoBeauty userInfoBeauty){
        userInfoBeautyService.saveBeautyAccount(userInfoBeauty);
        return ResponseVo.getSuccessResponseVo(null);

    }
    //@RequestParam("id")
    @Operation(summary = "delBeautyAccount",description = "删除靓号")
    @GetMapping("delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo delBeautyAccount(@NotNull Integer id){
        //已经在使用的不能删除
        UserInfoBeauty byId = userInfoBeautyService.getById(id);
        if(byId.getStatus().equals(BeautyAccountStatusEnum.USED.getStatus())){
          throw new BusinessException("已经被使用，无法删除");
        }else {
            userInfoBeautyService.removeById(id);
        }

        return ResponseVo.getSuccessResponseVo(null);

    }
}
