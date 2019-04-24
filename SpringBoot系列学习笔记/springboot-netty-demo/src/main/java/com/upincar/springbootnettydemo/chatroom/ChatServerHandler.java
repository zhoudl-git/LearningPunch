package com.upincar.springbootnettydemo.chatroom;

/**
 * @author: zhoudongliang
 * @date: 2018/9/19 17:49
 * @description: 回调处理类,继承SimpleChannelInboundHandler处理出站入站数据，模板设计模式，让主要的处理逻辑保持不变，让变化的步骤通过接口实现来完成
 */
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler <String>{


    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 当有客户端连接时，handlerAdded会执行,就把该客户端的通道记录下来，加入队列
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();//获得客户端通道
        //通知其他客户端有新人进入
        for (Channel channel : channels){
            if (channel != inComing)
                channel.writeAndFlush("[欢迎: " + inComing.remoteAddress() + "] 进入聊天室！\n");
        }

        channels.add(inComing);//加入队列
    }

    /**
     * 断开连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel outComing = ctx.channel();//获得客户端通道
        //通知其他客户端有人离开
        for (Channel channel : channels){
            if (channel != outComing)
                channel.writeAndFlush("[再见: ]" + outComing.remoteAddress() + " 离开聊天室！\n");
        }

        channels.remove(outComing);
    }

    /**
     * 每当从客户端有消息写入时
     * @param channelHandlerContext
     * @param s
     * @throws Exception
     */
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        Channel inComing = channelHandlerContext.channel();

        for (Channel channel : channels){
            if (channel != inComing){
                channel.writeAndFlush("[用户" + inComing.remoteAddress() + " 说：]" + s + "\n");
            }else {
                channel.writeAndFlush("[我说：]" + s + "\n");
            }
        }
    }

    /**
     * 当服务器监听到客户端活动时
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println("[" + inComing.remoteAddress() + "]: 在线");
    }

    /**
     * 离线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println("[" + inComing.remoteAddress() + "]: 离线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println(inComing.remoteAddress() + "通讯异常！");
        ctx.close();
    }
}
