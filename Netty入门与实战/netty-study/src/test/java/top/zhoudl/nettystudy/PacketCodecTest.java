package top.zhoudl.nettystudy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Assert;
import org.junit.Test;
import top.zhoudl.nettystudy.protocol.command.LoginRequestPacket;
import top.zhoudl.nettystudy.protocol.command.Packet;
import top.zhoudl.nettystudy.protocol.command.PacketCodec;
import top.zhoudl.nettystudy.protocol.serialize.Serializer;
import top.zhoudl.nettystudy.protocol.serialize.impl.JSONSerializationImpl;

/**
 * @author: zhoudongliang
 * @date: 2019/5/27 13:36
 * @description: 测试类
 */
public class PacketCodecTest {

    @Test
    public void encode() {

        Serializer serializer = new JSONSerializationImpl();
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();

        loginRequestPacket.setVersion(((byte) 1));
        loginRequestPacket.setUserId(123);
        loginRequestPacket.setUserName("zhoudl");
        loginRequestPacket.setPassword("pwd");


        PacketCodec packetCodec = new PacketCodec();
        ByteBuf byteBuf = packetCodec.encode(ByteBufAllocator.DEFAULT,loginRequestPacket);
        Packet decodedPacket = packetCodec.decode(byteBuf);

        Assert.assertArrayEquals(serializer.serialization(loginRequestPacket), serializer.serialization(decodedPacket));

    }

}
