package com.atmiao.wechatdemo.mapper;

import com.atmiao.wechatdemo.pojo.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author musichao
* @description 针对表【chat_message】的数据库操作Mapper
* @createDate 2024-06-27 11:14:32
* @Entity com.atmiao.wechatdemo.pojo.ChatMessage
*/
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    List<ChatMessage> selectMessageList(@Param("query") ChatMessage chatMessage);

    int updateStatusByMessageIdAndStatus(@Param("status") Integer status, @Param("messageId") Long messageId, @Param("oldStatus") Integer oldStatus);

}




