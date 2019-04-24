package com.upincar.springbootnettydemo.netty03;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author: zhoudongliang
 * @date: 2018/9/27 11:02
 * @description: Writing a Discard Server 写一个丢弃所有数据的服务器
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    // DiscardServerHandler extends ChannelInboundHandlerAdapter，
    // 这是一个实现ChannelInboundHandler。ChannelInboundHandler提供可以覆盖的各种事件处理程序方法。
    // 目前，只需自己扩展ChannelInboundHandlerAdapter而不是实现处理程序接口。


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Discard the received data silently.
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) {
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
