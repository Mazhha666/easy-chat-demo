package com.atmiao.wechatdemo.mapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;

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
    UserInfo queryOneByEmail(@Param("email") String email);

    UserInfo queryOneByUserId(@Param("userId") String userId);

    int updateLastLoginTimeByUserId(@Param("lastLoginTime") LocalDateTime lastLoginTime, @Param("userId") String userId);

    int updateLastOffTimeByUserId(@Param("lastOffTime") Long lastOffTime, @Param("userId") String userId);



}




