package com.qf.netty;

import com.qf.handler.ServerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

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
public class NettyServer {
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
                        //添加处理器链的成员
                        //添加行处理器，按行拆包、粘包
                        pipeline.addLast(new LineBasedFrameDecoder(1024*1024));
                        //String编解码器
                        pipeline.addLast(new StringDecoder()); //bytebuf→string
                        pipeline.addLast(new StringEncoder()); //string→bytebyf

                        pipeline.addLast(new ServerChannelHandler());
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
