package top.zhoudl.nettystudy.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import top.zhoudl.nettystudy.protocol.command.*;
import top.zhoudl.nettystudy.util.LoginUtil;

import java.util.Date;
import java.util.UUID;

/**
 * @author: zhoudongliang
 * @date: 2019/5/29 15:55
 * @description:
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + ": 客户端开始登录");

        // 创建登录对象
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        //loginRequestPacket.setUserId(UUID.randomUUID().toString());
        //loginRequestPacket.setUsername("flash");
        loginRequestPacket.setPassword("pwd");

        // 编码
        ByteBuf buffer = PacketCodec.INSTANCE.encode(ctx.alloc(), loginRequestPacket);

        // 写数据
        ctx.channel().writeAndFlush(buffer);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        Packet packet = PacketCodec.INSTANCE.decode(byteBuf);

        if (packet instanceof LoginResponsePacket) {
            LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;

            if (loginResponsePacket.isSuccess()) {
                System.out.println(new Date() + ": 客户端登录成功");
                LoginUtil.markAsLogin(ctx.channel());
            } else {
                System.out.println(new Date() + ": 客户端登录失败，原因：" + loginResponsePacket.getReason());
            }
        } else if (packet instanceof MessageResposePacket) {
            MessageResposePacket messageResponsePacket = (MessageResposePacket) packet;
            System.out.println(new Date() + ": 收到服务端的消息: " + messageResponsePacket.getMessage());
        }
    }
}

