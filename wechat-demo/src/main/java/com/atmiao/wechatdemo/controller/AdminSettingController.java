package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.dto.SysSettingDto;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author miao
 * @version 1.0
 */
@RestController("adminSettingController")
@CrossOrigin
@Slf4j
@Tag(name = "管理员设置模块", description = "管理员设置等接口")
@RequestMapping("admin")
@Validated
public class AdminSettingController {
    @Autowired
    RedisComponent redisComponent;
    @Autowired
    AppConfig appConfig;

    @Operation(summary = "getSysSetting", description = "得到系统设置")
    @PostMapping("getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo getSysSetting() {
        SysSettingDto sysSetting = redisComponent.getSysSetting();
        return ResponseVo.getSuccessResponseVo(sysSetting);
    }

    @Operation(summary = "saveSysSetting", description = "保存系统设置")
    @PostMapping("saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo saveSysSetting(SysSettingDto sysSettingDto,
                                    MultipartFile avatarFile,
                                  MultipartFile avatarCover) throws IOException {
        if (null != avatarCover) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + Constants.ROBOT_UID + Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }
        redisComponent.saveSysSetting(sysSettingDto);
        return ResponseVo.getSuccessResponseVo(null);
    }

}
