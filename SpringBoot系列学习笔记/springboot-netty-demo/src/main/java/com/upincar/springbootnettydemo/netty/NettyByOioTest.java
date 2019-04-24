package com.upincar.springbootnettydemo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author: zhoudongliang
 * @date: 2018/9/19 14:26
 * @description: 使用Netty完成一个阻塞的数据传输案例
 */
public class NettyByOioTest {

    public static void server(int port) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello 我是服务端\r\n", Charset.forName("UTF-8")));
        EventLoopGroup group = new OioEventLoopGroup();
        try {
            // 创建Server-Bootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    // 使用OioEventLoopGroup以允许阻塞模式（旧的I/O）
                    .channel(OioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    // 指定Channel-Initializer，对于每个已接受的连接都调用它
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    // 添加一个Channel-InboundHandler-Adapter 以拦截和处理事件
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext channelHandlerContext) {
                                            // 将消息写到客户端，并添加ChannelFutureListener，以便消息一被写完就关闭连接
                                            channelHandlerContext.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                        }
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg){
                                            SubscribeResp resp = new SubscribeResp();
                                            resp.setnSubReqID(555);
                                            resp.setRespCode(0);
                                            resp.setDesc("-------Netty book order succeed, 3days later, sent to the designated address");
                                            // 反馈消息
                                            ctx.writeAndFlush(resp);
                                            // 订购内容
                                            SubscribeReq req = (SubscribeReq)msg;
                                            if("XXYY".equalsIgnoreCase(req.getUserName())){
                                                System.out.println("接收到的数据: [  " + req.toString() + "   ]");
                                            }
                                        }

                                        @Override
                                        public  void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
                                            System.out.println("---------------exceptionCaught 网络异常，关闭网络");
                                            cause.printStackTrace();
                                            ctx.close();
                                        }

                                    });
                        }
                    });
            // 绑定服务器以接受连接
            ChannelFuture f = serverBootstrap.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            // 释放所有资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            server(10086);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
