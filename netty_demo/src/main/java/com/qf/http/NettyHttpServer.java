package com.qf.http;

import com.qf.handler.HttpChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Author zhangjun
 * @Date 2020/3/31
 * netty服务器
 *
 * ServerBootstrap - 服务端引导对象
 * bootstrap - 客户端引导对象
 * channel - 服务器/客户端之间发送消息的管道对象
 * ByteBuf - 消息的载体
 */
public class NettyHttpServer {
    public static void main(String[] args) {
        //创建netty服务器的初始化引导对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        //创建主从线程对象
        EventLoopGroup master = new NioEventLoopGroup();
        EventLoopGroup slave = new NioEventLoopGroup();

        //配置引导对象
        //1，配置当前netty的线程模型
        serverBootstrap.group(master,slave)
                //2，设置channel管道类型,与客户端区别开
                .channel(NioServerSocketChannel.class)
                //3，设置事件处理器，与客户端区别开
                .childHandler(new ChannelInitializer() {
                    protected void initChannel(Channel channel) throws Exception {
                        //获取事件处理器链对象
                        ChannelPipeline pipeline = channel.pipeline();
                        //添加处理器链的成员  创建一个http服务器
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(1024*1024));
                        pipeline.addLast(new HttpChannelHandler());

                    }
                });

        //绑定端口,此步骤是个异步的过程，可能会出现问题
        ChannelFuture future = serverBootstrap.bind(8080);
        try {
            //设置端口同步
            future.sync();
            System.out.println("端口绑定完成，netty服务器已启动");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
