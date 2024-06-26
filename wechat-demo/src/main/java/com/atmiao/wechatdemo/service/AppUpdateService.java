package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.pojo.AppUpdate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author musichao
* @description 针对表【app_update】的数据库操作Service
* @createDate 2024-06-23 15:07:32
*/
public interface AppUpdateService extends IService<AppUpdate> {

    Page<AppUpdate> loadUpdateList();

    void saveUpdate(AppUpdate appUpdate, MultipartFile file);

    void postUpdate(Integer id, Integer status, String grayscaleUid);

    boolean isPubilshed(Integer id);

    AppUpdate getLatestUpdate(String appVersion, String uid);
}
