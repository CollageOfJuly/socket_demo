package com.qf.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @Author zhangjun
 * @Date 2020/3/31
 * 服务端入站消息处理器
 */
@ChannelHandler.Sharable
public class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    String path = "D:\\ChromeDownloads";

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {

        //判断当前的请求
        if (!fullHttpRequest.method().name().equals("GET")){
            //返回一个http错误页面
            setError(ctx,"请发送GET请求！");
            return;
        }

        //获取当前访问路径
        String uri=fullHttpRequest.uri();
        //中文处理
        uri = URLDecoder.decode(uri, "UTF-8");
        System.out.println("uri为"+uri);
        File file=new File(path,uri);
        System.out.println(file.getAbsolutePath());
        if (!file.exists()){
            //路径不存在
            setError(ctx,"访问的路径不存在，请不要乱来！");
            return;
        }
        //判断当前路径是文件夹还是文件
        if (file.isDirectory()){ //是文件夹
            //返回http响应体内容列表
            showContext(ctx,file,uri);
        }else {
            //按照文件处理，下载文件
            fileHandler(ctx, file);
        }


    }

    /**
     * 处理文件
     */
    private void fileHandler(final ChannelHandlerContext ctx, File file){


        //将file文件下载到客户端


        //准备下载的响应
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        //设置下载的响应头
        response.headers().add("Content-Type", "application/octet-stream");  //内容类型
        response.headers().add("Content-Length", file.length());   //内容长度


        //直接返回Response对象，告诉浏览器下载信息
        ctx.writeAndFlush(response);

        //设置下载速率
        //文件的下载，还需要加个分块下载处理器链:pipeline.addLast(new ChunkedWriteHandler());//位置在请求编码处理器链的前面
        try {
            ChunkedNioFile nioFile = new ChunkedNioFile(file, 1024 * 1024); //1024*1024表示下载的效率为1M
            ChannelFuture future = ctx.writeAndFlush(nioFile);  //下载过程中为异步操作，需要设置同步，防止下载过程中关闭了连接！
            future.addListener(new ChannelFutureListener() {   //等同于future.sync()
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        System.out.println("下载完成，关闭连接！");
                        ctx.close();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 错误页面
     * @param ctx
     * @param error
     */
    private void setError(ChannelHandlerContext ctx,String error){
        //返回一个html页面
        //1,创建一个http响应对象
        FullHttpResponse fullHttpResponse=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        //2,设置响应头部
        fullHttpResponse.headers().add("Context-Type","text/html;charset=utf-8");
        //3,设置响应体内容
        try {
            fullHttpResponse.content().writeBytes(("<html><head><meta charset=\"UTF-8\"></head><body><h1>" + error + "</h1></body></html>").getBytes("utf-8"));
            //4，发送响应体
            ctx.writeAndFlush(fullHttpResponse);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            //5，关闭连接
            ctx.close();
        }
    }

    /**
     * 展示文件夹内容
     * @param ctx
     * @param file
     */
    private void showContext(ChannelHandlerContext ctx,File file,String uri){
        //返回一个html页面
        //1，创建响应对象
        FullHttpResponse fullHttpResponse=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        //2,设置响应对象头部信息
        fullHttpResponse.headers().add("Context-Type","text/html;charset=utf-8");
        //3,设置响应体内容
        String context="<html><head><meta charset=\"UTF-8\"></head><body><ul>";
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (uri.endsWith("/")){ //判断当前路径是否为根路径，如果是根路径则url为/；如果不是根路径则url不会以/结尾
                context+="<li><a href='http://localhost:8080"+uri+file1.getName()+"'>("+(file1.isFile() ? "文件" : "文件夹")+")"+file1.getName()+"</a></li>";
            }else {
                context+="<li><a href='http://localhost:8080"+uri+"/"+file1.getName()+"'>("+(file1.isFile() ? "文件" : "文件夹")+")"+file1.getName()+"</a></li>";
            }
        }
        context+="</ul></body></html>";
        try {
            fullHttpResponse.content().writeBytes(context.getBytes("UTF-8"));
            //4,发送响应体
            ctx.writeAndFlush(fullHttpResponse);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            //5，关闭连接
            ctx.close();
        }
    }
}
