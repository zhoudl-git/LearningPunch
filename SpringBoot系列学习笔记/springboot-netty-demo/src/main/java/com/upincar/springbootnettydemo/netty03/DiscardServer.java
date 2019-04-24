package com.upincar.springbootnettydemo.netty03;

/**
 * @author: zhoudongliang
 * @date: 2018/9/27 13:34
 * @description:
 */

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Discards any incoming data.
 */
public class DiscardServer {

    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        //1 创建线两个程组
        //一个是用于处理服务器端接收客户端连接的
        //一个是进行网络通信的（网络读写的）
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //2 创建辅助工具类，用于服务器通道的一系列配置
            ServerBootstrap b = new ServerBootstrap();
            //绑定俩个线程组
            b.group(bossGroup, workerGroup)
                    //指定NIO的模式
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //3 在这里配置具体数据接收方法的处理
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    //设置tcp缓冲区
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //保持连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //设置发送缓冲大小
                    .option(ChannelOption.SO_SNDBUF, 32*1024)
                    //设置接收缓冲大小
                    .option(ChannelOption.SO_RCVBUF, 32*1024);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        /*if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }*/
        new DiscardServer(port).run();
    }
}
