package com.atmiao.wechatdemo.websosket.netty;

import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.utils.JsonUtils;
import com.atmiao.wechatdemo.websosket.ChannelContextUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author miao
 * @version 1.0
 */
@Component
@Slf4j
public class MessageHandler {
    private static final String MESSAGE_TOPIC = "message.topic";
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private ChannelContextUtils channelContextUtils;
    //服务启动，同时redisson启动监听
    @PostConstruct
    public void listenMessage(){
        RTopic topic = redissonClient.getTopic(MESSAGE_TOPIC);
        topic.addListener(MessageSendDto.class,(MessageSendDto,sendDto) ->{
//            log.info("收到广播消息:{}", JsonUtils.toJSONString(sendDto));
           channelContextUtils.sendMessageByType(sendDto);
        });

    }
    public void sendMessage(MessageSendDto messageSendDto){
        RTopic topic = redissonClient.getTopic(MESSAGE_TOPIC);
        topic.publish(messageSendDto);
    }
}
