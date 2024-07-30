package com.atmiao.wechatdemo.websosket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author miao
 * @version 1.0
 */
@Slf4j
public class HandlerHeartBeat extends ChannelDuplexHandler {
    /**
     *
     * @param ctx
     * @param evt
     * @throws Exception 处理超时事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent e = (IdleStateEvent) evt;
            if(e.state() == IdleState.READER_IDLE){
                Channel channel = ctx.channel();
                Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
                String userId = attribute.get();
                log.info("用户{}心跳超时",userId);
                ctx.close();
            }else if(e.state() == IdleState.WRITER_IDLE){
                ctx.writeAndFlush("heart");
            }

        }
    }
}
