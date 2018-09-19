package com.upincar.springbootnettydemo.netty;

/**
 * @author: zhoudongliang
 * @date: 2018/9/19 16:30
 * @description:
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class SubReqClient {

    public void connect(int nPort, String strHost) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,  true)
                    .handler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception{
                            ch.pipeline().addLast(
                                    new ObjectDecoder(1024, ClassResolvers
                                            .cacheDisabled(this.getClass().getClassLoader())));

                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new SubReqClientHanler());
                        }
                    });

            ChannelFuture f = b.connect(strHost,  nPort).sync();

            f.channel().closeFuture().sync();

        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("---------------- 获取 channel 异常 ----------------");
        }finally{

        }
    }

    public static void main(String[] args){
        int nPort = 8082;
        String strHost = "127.0.0.1";
        try {
            System.out.println("----------------开始连接----------------");
            new SubReqClient().connect(nPort, strHost);
        } catch (Exception e) {
            System.out.println("----------------连接异常----------------");
            e.printStackTrace();
        }
    }
}
