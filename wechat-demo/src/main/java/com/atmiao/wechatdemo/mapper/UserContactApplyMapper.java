package com.atmiao.wechatdemo.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.atmiao.wechatdemo.pojo.UserContactApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author musichao
* @description 针对表【user_contact_apply】的数据库操作Mapper
* @createDate 2024-06-12 18:28:24
* @Entity com.atmiao.wechatdemo.pojo.UserContactApply
*/
@Mapper
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {
    UserContactApply queryOneByApplyUserIdAndContactId(@Param("applyUserId") String applyUserId, @Param("contactId") String contactId);
    Page<UserContactApply> loadApply(Page<UserContactApply> page, @Param("userId")String userId,@Param("queryContactInfo")Boolean queryContactInfo);
    List<UserContactApply> queryInThreeDaysApply(@Param("query") UserContactApply userContactApply);
}




