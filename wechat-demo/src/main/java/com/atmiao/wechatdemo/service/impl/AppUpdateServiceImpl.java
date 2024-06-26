package com.atmiao.wechatdemo.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.commons.Constants;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.enums.AppUpdateFileTypeEnum;
import com.atmiao.wechatdemo.commons.enums.AppUpdateStatusEnum;
import com.atmiao.wechatdemo.config.AppConfig;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.AppUpdate;
import com.atmiao.wechatdemo.service.AppUpdateService;
import com.atmiao.wechatdemo.mapper.AppUpdateMapper;
import org.aspectj.lang.annotation.AfterThrowing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author musichao
 * @description 针对表【app_update】的数据库操作Service实现
 * @createDate 2024-06-23 15:07:32
 */
@Service
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate>
        implements AppUpdateService {
    @Autowired
    private AppUpdateMapper appUpdateMapper;
    @Autowired
    private AppConfig appConfig;

    @Override
    public Page<AppUpdate> loadUpdateList() {
        //TODO 需要后续修改
        Page<AppUpdate> appUpdatePage = new Page<>(0, -1);
        LambdaQueryWrapper<AppUpdate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AppUpdate::getId);
        Page<AppUpdate> appUpdatePage1 = appUpdateMapper.selectPage(appUpdatePage, wrapper);
        return appUpdatePage1;
    }

    @Override
    public void saveUpdate(AppUpdate appUpdate, MultipartFile file) {
        AppUpdateFileTypeEnum typeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
        if (typeEnum == null) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        //已经发布的不能修改
        if(appUpdate.getId() != null && isPubilshed(appUpdate.getId())){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        //当前版本号无论新增或修改都要大于数据库最新的版本
        LambdaQueryWrapper<AppUpdate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AppUpdate::getId).last("limit 1");
        List<AppUpdate> appUpdateList = appUpdateMapper.selectList(wrapper);
        if (!appUpdateList.isEmpty()) {
            AppUpdate latest = appUpdateList.get(0);
            Long dbVersion = Long.parseLong(latest.getVersion().replace(".", ""));
            Long currentVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));
            if (appUpdate.getId() == null && currentVersion <= dbVersion) {
                throw new BusinessException("版本号设置必须大于" + latest.getVersion());
            }
            //只能修改以前的,最大的可以调上线，其他的不行
            if (appUpdate.getId() != null && currentVersion >= dbVersion && !appUpdate.getId().equals(latest.getId())) {
                throw new BusinessException("修改版本号设置必须小于" + latest.getVersion());
            }
            AppUpdate versionDb = appUpdateMapper.queryOneByVersion(appUpdate.getVersion());
            //修改版本号不能已经存在,可以修改自己
            if (appUpdate.getId() != null && versionDb != null && !versionDb.getId().equals(appUpdate.getId())) {
                throw new BusinessException("版本号已经存在");
            }
        }
        //插入或修改
        if (appUpdate.getId() == null) {
            appUpdate.setCreateTime(LocalDateTime.now());
            appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
            appUpdateMapper.insert(appUpdate);
        } else {
//            appUpdate.setStatus(null);
//            appUpdate.setGrayscaleUid(null);
            appUpdateMapper.updateById(appUpdate);
        }
        if (file != null) {
            File path = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER);
            if (!path.exists()) {
                path.mkdirs();
            }
            try {
                file.transferTo(new File(path.getAbsolutePath() + "/" + appUpdate.getId() + Constants.APP_EXE_SUFFIX));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByType(status);
        if (statusEnum == null) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if (AppUpdateStatusEnum.GRAYSCALE == statusEnum && StringUtils.isEmpty(grayscaleUid)) {
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        if(AppUpdateStatusEnum.GRAYSCALE != statusEnum){
            grayscaleUid = null;
        }
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setStatus(status);
        appUpdate.setGrayscaleUid(grayscaleUid);
        appUpdateMapper.updateById(appUpdate);
    }

    @Override
    public boolean isPubilshed(Integer id) {
        AppUpdate dbId = appUpdateMapper.selectById(id);
        if(dbId != null && !AppUpdateStatusEnum.INIT.getStatus().equals(dbId.getStatus())){
            return true;
        }
        return false;
    }

    @Override
    public AppUpdate getLatestUpdate(String appVersion, String uid) {
        AppUpdate appUpdate = appUpdateMapper.selectLatestUpdate(appVersion, uid);
        return appUpdate;
    }
}




