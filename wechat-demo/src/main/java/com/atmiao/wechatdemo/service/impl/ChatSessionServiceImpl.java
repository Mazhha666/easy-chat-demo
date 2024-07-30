package com.atmiao.wechatdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atmiao.wechatdemo.pojo.ChatSession;
import com.atmiao.wechatdemo.service.ChatSessionService;
import com.atmiao.wechatdemo.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

/**
* @author musichao
* @description 针对表【chat_session】的数据库操作Service实现
* @createDate 2024-06-27 11:14:39
*/
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService{

}




