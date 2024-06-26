package com.atmiao.wechatdemo.mapper;
import org.apache.ibatis.annotations.Param;

import com.atmiao.wechatdemo.pojo.AppUpdate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author musichao
* @description 针对表【app_update】的数据库操作Mapper
* @createDate 2024-06-23 15:07:32
* @Entity com.atmiao.wechatdemo.pojo.AppUpdate
*/
public interface AppUpdateMapper extends BaseMapper<AppUpdate> {
    AppUpdate queryOneByVersion(@Param("version") String version);


    AppUpdate selectLatestUpdate(@Param("appVersion") String appVersion, @Param("uid") String uid);
}




