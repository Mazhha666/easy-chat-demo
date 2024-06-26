package com.atmiao.wechatdemo.mapper;

import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.UserContact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author musichao
* @description 针对表【user_contact】的数据库操作Mapper
* @createDate 2024-06-12 18:28:24
* @Entity com.atmiao.wechatdemo.pojo.UserContact
*/
@Mapper
public interface UserContactMapper extends BaseMapper<UserContact> {
    List<UserContact> queryGroups(@Param("groupId") String groupId,
                                  @Param("status") Integer status,
                                  @Param("queryUserInfo")Boolean queryUserInfo);

    UserContact queryOneByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);
    List<UserContact> loadContact(@Param("query") UserContact userContact);
}




