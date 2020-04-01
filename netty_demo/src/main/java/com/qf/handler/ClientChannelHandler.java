package com.qf.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author zhangjun
 * @Date 2020/3/31
 */
@ChannelHandler.Sharable
public class ClientChannelHandler extends SimpleChannelInboundHandler<String> {
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println("接收到的服务端消息："+s);
    }
}
