package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.dto.MessageSendDto;
import com.atmiao.wechatdemo.websosket.netty.MessageHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author miao
 * @version 1.0
 */
@RestController
@CrossOrigin
@Slf4j
@Tag(name = "hello",description = "这是一个测试接口")
@RequestMapping("test")
public class HelloConrtroller {
    @Resource
    private MessageHandler messageHandler;
    @Operation(summary = "hello",description = "hello的方法")
    @GetMapping("hello")
    public String sayHello(){
        log.info("hh");
        return "hello world";
    }
    @Operation(summary = "testRedisClient",description = "testRedisClient的方法")
    @GetMapping("testRedisClient")
    public ResponseVo testRedisClient(){
        MessageSendDto<Object> objectMessageSendDto = new MessageSendDto<>();
        objectMessageSendDto.setMessageContent("hello server" + System.currentTimeMillis());
        messageHandler.sendMessage(objectMessageSendDto);
        return ResponseVo.getSuccessResponseVo(null);
    }
    @GetMapping("response")
    public ResponseVo<Object> testResponse(){
        return ResponseVo.getSuccessResponseVo(null);
    }

}
