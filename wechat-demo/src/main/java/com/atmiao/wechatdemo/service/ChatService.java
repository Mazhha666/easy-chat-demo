package com.atmiao.wechatdemo.service;


import com.atmiao.wechatdemo.chatDomain.ChatResponse;
import reactor.core.publisher.Mono;

/**
 * @author miao
 * @version 1.0
 */

public interface ChatService {
    Mono<ChatResponse> getChatCompletion(String model, String userMessage);
//    @PostExchange(url="https://spark-api-open.xf-yun.com/v1/chat/completions",accept = "application/json")
//    public Mono<String> chatWithRobot(@RequestParam(value = "model",required = false,defaultValue = "4.0Ultra")String model,@RequestParam("messages")String messsages);
}
