package com.atmiao.wechatdemo;

import com.atmiao.wechatdemo.websosket.netty.NettyWebSocketStarter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan(basePackages = "com.atmiao.wechatdemo.mapper")
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class WechatDemoApplication {


    public static void main(String[] args) {
        SpringApplication.run(WechatDemoApplication.class, args);

    }

}
