package com.atmiao.wechatdemo.mapper;

import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author musichao
* @description 针对表【group_info】的数据库操作Mapper
* @createDate 2024-06-12 18:28:24
* @Entity com.atmiao.wechatdemo.pojo.GroupInfo
*/
@Mapper
public interface GroupInfoMapper extends BaseMapper<GroupInfo> {
    Page<GroupInfo> loadGroup(Page<GroupInfo> groupInfoPage, @Param("query")GroupInfo groupInfo);
}




