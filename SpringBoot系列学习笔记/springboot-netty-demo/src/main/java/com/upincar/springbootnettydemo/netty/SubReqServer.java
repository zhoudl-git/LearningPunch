package com.upincar.springbootnettydemo.netty;

/**
 * @author: zhoudongliang
 * @date: 2018/9/19 16:24
 * @description:
 */
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;

public class SubReqServer {

    public void bind(int nPort) throws Exception {

        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello 我是服务端\r\n", Charset.forName("UTF-8")));

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception{
                            ch.pipeline()
                                    .addLast(
                                            new ObjectDecoder(1024*1024,
                                                    ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())))
                                    .addLast(new ObjectEncoder())
                                    .addLast(
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
                                                    resp.setDesc("-------Netty 数据发送 --------");
                                                    // 反馈消息
                                                    ctx.writeAndFlush(resp);
                                                    // 订购内容
                                                    SubscribeReq req = (SubscribeReq)msg;
                                                    System.out.println("接收到的数据: [  " + req.toString() + "   ]");
                                                }

                                                @Override
                                                public  void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
                                                    System.out.println("---------------exceptionCaught 网络异常，关闭网络----------------");
                                                    cause.printStackTrace();
                                                    ctx.close();
                                                }

                                            });
                        }
                    });

            ChannelFuture f = b.bind(nPort).sync();
            System.out.println("---------------等待客户端连接----------------");
            f.channel().closeFuture().sync();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("---------------等待客户端连接异常!----------------");
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args){
        int nPort = 10086;
        nPort = Integer.valueOf(nPort);
        System.out.println("---------------服务端开始启动----------------");
        try {
            new SubReqServer().bind(nPort);
        } catch (Exception e) {
            System.out.println("---------------服务端启动异常----------------");
            e.printStackTrace();
        }
    }
}

