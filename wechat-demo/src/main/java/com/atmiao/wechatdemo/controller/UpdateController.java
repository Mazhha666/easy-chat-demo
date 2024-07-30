package com.atmiao.wechatdemo.controller;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.commons.enums.AppUpdateFileTypeEnum;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.pojo.AppUpdate;
import com.atmiao.wechatdemo.pojo.AppUpdateVo;
import com.atmiao.wechatdemo.service.AppUpdateService;
import com.atmiao.wechatdemo.utils.CopyUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;

/**
 * @author miao
 * @version 1.0
 */
@RestController("updateController")
@CrossOrigin
@Slf4j
@Tag(name = "检测更新模块", description = "检测更新")
@RequestMapping("update")
@Validated
public class UpdateController {
    @Autowired
    private AppUpdateService appUpdateService;
    @Autowired
    private AppConfig appConfig;

    /**
     * 更新检测 启动时自动调用，可以关闭，可以客户端自己检测更新
     * @param appVersion
     * @param uid
     * @return
     */
    @Operation(summary = "checkVersion", description = "检测更新")
    @PostMapping("checkVersion")
    @GlobalInterceptor
    public ResponseVo checkVersion(@RequestParam(value = "appVersion",required = false) String appVersion,
                                   @RequestParam(value = "uid",required = false) String uid) {

        if(StringUtils.isEmpty(appVersion)){
            return ResponseVo.getSuccessResponseVo(null);
        }
        AppUpdate latestUpdate = appUpdateService.getLatestUpdate(appVersion,uid);
        if(latestUpdate == null){
            return ResponseVo.getSuccessResponseVo(null);
        }
        AppUpdateVo appUpdateVo = CopyUtil.copy(latestUpdate, AppUpdateVo.class);
        if(latestUpdate.getFileType().equals(AppUpdateFileTypeEnum.LOCAL.getType())){
            File file = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + latestUpdate.getId() + Constants.APP_EXE_SUFFIX);
            appUpdateVo.setSize(file.length());
        }else {
            appUpdateVo.setSize(0L);
        }
         appUpdateVo.setUpdateList(Arrays.asList(latestUpdate.getUpdateDescArray()));
        String fileName = Constants.APP_NAME + latestUpdate.getVersion() + Constants.APP_EXE_SUFFIX;
        appUpdateVo.setFileName(fileName);
        return ResponseVo.getSuccessResponseVo(appUpdateVo);
    }
}
