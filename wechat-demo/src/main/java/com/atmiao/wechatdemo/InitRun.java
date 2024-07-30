package com.atmiao.wechatdemo;

import com.atmiao.wechatdemo.websosket.netty.NettyWebSocketStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author miao
 * @version 1.0
 */
@Component("initRun")
public class InitRun implements ApplicationRunner {
    @Autowired
    private NettyWebSocketStarter nettyWebSocketStarter;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(nettyWebSocketStarter).start();
    }
}
