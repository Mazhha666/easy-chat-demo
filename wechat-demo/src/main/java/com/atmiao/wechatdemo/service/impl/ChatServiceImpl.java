package com.atmiao.wechatdemo.service.impl;



import com.atmiao.wechatdemo.chatDomain.ChatResponse;
import com.atmiao.wechatdemo.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ChatServiceImpl implements ChatService {

    private final WebClient webClient;

    public ChatServiceImpl() {
        this.webClient = WebClient.builder()
                .baseUrl("https://spark-api-open.xf-yun.com")
                .build();
    }

    public Mono<ChatResponse> getChatCompletion(String model, String userMessage) {
        // 构建请求体
        String requestBody = String.format("{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", model, userMessage);
        Mono<ChatResponse> authorization = webClient.post()
                .uri("/v1/chat/completions").accept(MediaType.ALL)
                .header("Authorization", "Bearer 28ecb75b9574141aa3fd587d88487638:YzY5MmE3ZDY2N2IzNmVlOGM1ODcyNTFl")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody) // 注意：这里使用了bodyValue而不是body(BodyInserters.fromValue(requestBody))，因为bodyValue是简化方法
                .retrieve()
                .bodyToMono(ChatResponse.class);
        // 发起POST请求
        return  authorization;// 假设响应是JSON字符串，你可以改为bodyToMono(YourResponseClass.class)来自动反序列化
    }

    // 注意：在实际应用中，你可能需要处理错误响应，例如使用.onStatus(...)或.onErrorResume(...)等方法
}