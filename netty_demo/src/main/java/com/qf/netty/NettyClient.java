package com.qf.netty;

import com.qf.handler.ClientChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Author zhangjun
 * @Date 2020/3/31
 * netty客户端
 */
public class NettyClient {
    public static void main(String[] args) {

        //创建netty客户端的初始化引导对象
        Bootstrap bootstrap = new Bootstrap();

        //配置引导对象
        bootstrap
                //设置多线程模型
                .group(new NioEventLoopGroup())
                //设置channel管道类型，与服务端区别开
                .channel(NioSocketChannel.class)
                //设置事件处理器，与服务端区别开
                .handler(new ChannelInitializer() {
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new ClientChannelHandler());
                    }
                });

        //连接服务器
        ChannelFuture future = bootstrap.connect("127.0.0.1", 8080);
        try {
            //设置同步
            future.sync();
            System.out.println("客户端连接netty服务器成功");

            //给服务器循环发送消息
//            Scanner scanner = new Scanner(System.in);
//            while (true) { //循环发送消息
//                System.out.println("请输入发送的内容：");
//                String context = scanner.next();
//                byte[] bytes = context.getBytes("UTF-8");
//
//                //1,创建管道对象
//                Channel channel = future.channel();
//                //2，创建ByteBuf
//                ByteBuf byteBuf = Unpooled.buffer(bytes.length);
//                byteBuf.writeBytes(bytes);
//                channel.writeAndFlush(byteBuf);
//            }
            for (int i = 0; i < 10; i++) {
                System.out.println("客户端发送消息："+(i+1));
                Channel channel = future.channel();
                String msg="消息"+(i+1)+"\r\n";
                channel.writeAndFlush(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
