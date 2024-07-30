package com.atmiao.wechatdemo.mapper;

import com.atmiao.wechatdemo.pojo.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author musichao
* @description 针对表【chat_session】的数据库操作Mapper
* @createDate 2024-06-27 11:14:39
* @Entity com.atmiao.wechatdemo.pojo.ChatSession
*/
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

}




