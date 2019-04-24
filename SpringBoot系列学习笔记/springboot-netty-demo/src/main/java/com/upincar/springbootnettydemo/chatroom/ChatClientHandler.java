package com.upincar.springbootnettydemo.chatroom;

/**
 * @author: zhoudongliang
 * @date: 2018/9/19 17:51
 * @description:
 */
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatClientHandler extends SimpleChannelInboundHandler<String> {

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        //直接输出消息
        System.out.println(s);
    }
}
