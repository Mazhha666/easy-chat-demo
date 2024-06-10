package com.atmiao.wechatdemo.mapper;

import com.atmiao.wechatdemo.pojo.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author musichao
* @description 针对表【user_info】的数据库操作Mapper
* @createDate 2024-06-05 21:00:20
* @Entity com.atmiao.wechatdemo.pojo.UserInfo
*/
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

}




