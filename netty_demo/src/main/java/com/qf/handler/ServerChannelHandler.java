package com.qf.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author zhangjun
 * @Date 2020/3/31
 * 服务端入站消息处理器
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends SimpleChannelInboundHandler<String> {

    private static List<Channel> list = new ArrayList<Channel>();

    /**
     * 每有一个客户端连接都会触发该方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("有一个客户端连接了！");
        list.add(ctx.channel());
        System.out.println(list);
    }

    /**
     * 入站消息处理
     * @param ctx
     * @param str
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String str) throws Exception {
        System.out.println("接受到客户端的消息："+str);

        for (Channel channel : list) {
            if (channel != ctx.channel()){ //获取其他连接的channel对象
//                ByteBuf byteBuf1 = Unpooled.copiedBuffer(byteBuf);
//                channel.writeAndFlush(byteBuf1);
                channel.writeAndFlush(str);
            }
        }
    }

}
