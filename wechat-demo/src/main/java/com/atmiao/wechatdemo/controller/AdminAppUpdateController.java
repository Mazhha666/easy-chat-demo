package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.commons.enums.AppUpdateStatusEnum;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.pojo.AppUpdate;
import com.atmiao.wechatdemo.service.AppUpdateService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author miao
 * @version 1.0
 */
@RestController("adminAppUpdateController")
@CrossOrigin
@Slf4j
@Tag(name = "版本控制模块", description = "版本控制发布等接口")
@RequestMapping("admin")
@Validated
public class AdminAppUpdateController {
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private AppUpdateService appUpdateService;
    @Operation(summary = "loadUpdateList", description = "获取更新列表")
    @PostMapping("loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo loadUpdateList() {
        Page<AppUpdate> page = appUpdateService.loadUpdateList();
        return ResponseVo.getSuccessResponseVo(page);
    }
    @Operation(summary = "saveUpdate", description = "保存更新")
    @PostMapping("saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo saveUpdate(@RequestParam(value = "id",required = false) Integer id,//修改或者新增
                                 @RequestParam("version") @NotEmpty String version,
                                 @RequestParam("updateDesc") @NotEmpty String updateDesc,
                                 @RequestParam("fileType") @NotNull Integer fileType,
                                 @RequestParam("outerLink")String outerLink,
                                 @RequestParam(value = "file",required = false) MultipartFile file) {
        //避免了封装vo导致的status个greyScale穿透修改
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setOuterLink(outerLink);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setFileType(fileType);
        appUpdateService.saveUpdate(appUpdate,file);
        return ResponseVo.getSuccessResponseVo(null);
    }
    @Operation(summary = "delUpdate", description = "删除版本更新")
    @PostMapping("delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo delUpdate(@RequestParam("id") @NotNull Integer id) {
        //已经发布的不能删除
        if(!appUpdateService.isPubilshed(id)) {
            appUpdateService.removeById(id);
        }else {
            throw new BusinessException("已经发布。无法删除");
        }
        return ResponseVo.getSuccessResponseVo(null);
    }
    @Operation(summary = "postUpdate", description = "发布版本更新")
    @PostMapping("postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo postUpdate(@RequestParam("id") @NotNull Integer id,
                                 @RequestParam("status") @NotNull Integer status,
                                @RequestParam(value = "grayscaleUid",required = false) String grayscaleUid) {
        appUpdateService.postUpdate(id,status,grayscaleUid);
        return ResponseVo.getSuccessResponseVo(null);
    }

}
