package com.atmiao.wechatdemo.service;

import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.GroupMFVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author musichao
* @description 针对表【group_info】的数据库操作Service
* @createDate 2024-06-12 18:28:24
*/
public interface GroupInfoService extends IService<GroupInfo> {

    void saveGroup(GroupMFVo groupMFVo, TokenUserInfoDto tokenUserInfoDto);

    List<GroupInfo> loadMyGroup(String userId);

    Page<GroupInfo> loadGroup();

    void distributeGroup(String groupOwnerId, String groupId);
}
