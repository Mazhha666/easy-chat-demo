package com.atmiao.wechatdemo.mapper;
import org.apache.ibatis.annotations.Param;

import com.atmiao.wechatdemo.pojo.UserInfoBeauty;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author musichao
* @description 针对表【user_info_beauty】的数据库操作Mapper
* @createDate 2024-06-05 21:01:13
* @Entity com.atmiao.wechatdemo.pojo.UserInfoBeauty
*/
@Mapper
public interface UserInfoBeautyMapper extends BaseMapper<UserInfoBeauty> {
    UserInfoBeauty queryOneByEmail(@Param("email") String email);

    UserInfoBeauty queryOneByUserId(@Param("userId") String userId);

}




