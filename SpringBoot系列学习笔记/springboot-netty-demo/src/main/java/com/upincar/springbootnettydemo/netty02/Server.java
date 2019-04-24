package com.upincar.springbootnettydemo.netty02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author: zhoudongliang
 * @date: 2018/9/26 16:23
 * @description:
 */
public class Server {

    public static void main(String[] args) throws Exception {
        //1 创建线两个程组
        //一个是用于处理服务器端接收客户端连接的
        //一个是进行网络通信的（网络读写的）
        EventLoopGroup pGroup = new NioEventLoopGroup();
        EventLoopGroup cGroup = new NioEventLoopGroup();

        //2 创建辅助工具类，用于服务器通道的一系列配置
        ServerBootstrap b = new ServerBootstrap();
        //绑定俩个线程组
        b.group(pGroup, cGroup)
                //指定NIO的模式
                .channel(NioServerSocketChannel.class)
                //设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                //设置发送缓冲大小
                .option(ChannelOption.SO_SNDBUF, 32*1024)
                //这是接收缓冲大小
                .option(ChannelOption.SO_RCVBUF, 32*1024)
                //保持连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        //3 在这里配置具体数据接收方法的处理
                        sc.pipeline().addLast(new ServerHandler());
                    }
                });

        //4 进行绑定
        ChannelFuture cf1 = b.bind(8765).sync();
        //ChannelFuture cf2 = b.bind(8764).sync();
        //5 等待关闭
        cf1.channel().closeFuture().sync();
        //cf2.channel().closeFuture().sync();
        pGroup.shutdownGracefully();
        cGroup.shutdownGracefully();
    }
}
