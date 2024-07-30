package com.atmiao.wechatdemo.websosket.netty;

import com.alibaba.druid.util.StringUtils;
import com.atmiao.wechatdemo.dto.TokenUserInfoDto;
import com.atmiao.wechatdemo.utils.RedisComponent;
import com.atmiao.wechatdemo.websosket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author miao
 * @version 1.0
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private ChannelContextUtils channelContextUtils;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
//        log.info("收到的消息{}",textWebSocketFrame.text());
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
//        log.info("收到userId{}的消息{}",userId,textWebSocketFrame.text());
        redisComponent.saveUserHeartBeat(userId);

    }

    /**
     * 通道就绪后，调用，一般用户来做初始化
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接断开");
        channelContextUtils.removeContext(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            WebSocketServerProtocolHandler.HandshakeComplete complete =  (WebSocketServerProtocolHandler.HandshakeComplete)evt;
            String url = complete.requestUri();
            String token = getToken(url);
            if(token == null){
                ctx.channel().close();
                return;
            }
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if(tokenUserInfoDto == null){
                ctx.channel().close();
                return;
            }
            log.info("url{}",url);
            channelContextUtils.addContext(tokenUserInfoDto.getUserId(),ctx.channel());

        }
    }
    private String getToken(String url){
        if(StringUtils.isEmpty(url) || !url.contains("?")){
            return null;
        }
        String[] queryParams = url.split("\\?");
        if(queryParams.length != 2){
            return null;
        }
        String[] params = queryParams[1].split("=");
        if(params.length != 2){
            return  null;
        }
        return params[1];
    }
}
