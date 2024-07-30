package com.atmiao.wechatdemo.mapper;

import com.atmiao.wechatdemo.pojo.ChatSessionUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author musichao
* @description 针对表【chat_session_user】的数据库操作Mapper
* @createDate 2024-06-27 11:14:53
* @Entity com.atmiao.wechatdemo.pojo.ChatSessionUser
*/
@Mapper
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {
        List<ChatSessionUser> querySessionUserList(@Param("userId")String userId);

        ChatSessionUser queryOneByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);

        int updateContactNameByUserIdAndContactId(@Param("contactName") String contactName, @Param("userId") String userId, @Param("contactId") String contactId);

        int updateContactNameByContactId(@Param("contactName") String contactName, @Param("contactId") String contactId);

        int delByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);


}




