package com.atmiao.wechatdemo.websosket.netty;

import com.atmiao.wechatdemo.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author miao
 * @version 1.0
 */
@Component
@Slf4j

public class NettyWebSocketStarter implements Runnable{
    private static EventLoopGroup  bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup = new NioEventLoopGroup();
    @Autowired
    private  HandlerWebSocket handlerWebSocket;
    @Autowired
    private AppConfig appConfig;
    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }


    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class).handler(
                            new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //设置几个重要的处理器
                            //对http协议的支持，使用http的编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            //聚合解码httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            //保证接收到的http请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            //心跳 long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit
                            //readerIdleTime 读超时时间，测试端一段时间内未接受被测试端消息

                            pipeline.addLast(new IdleStateHandler(6,0,0, TimeUnit.SECONDS));
                            pipeline.addLast(new HandlerHeartBeat());
                            //将http协议升级为ws协议，对websocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,64*1024,true,true,10000L));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            //wsPort 读取集群 而不是single
            String wsPortStr = System.getProperty("ws.port");
            Integer wsPort = appConfig.getWsPort();
            if(!StringUtils.isEmpty(wsPortStr)){
                wsPort = Integer.parseInt(wsPortStr);
            }
            ChannelFuture channelFuture =   serverBootstrap.bind(wsPort).sync();
            log.info("netty启动成功端口{}",appConfig.getWsPort());
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("启动netty失败");
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
