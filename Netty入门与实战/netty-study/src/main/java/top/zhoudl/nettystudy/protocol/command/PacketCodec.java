package top.zhoudl.nettystudy.protocol.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import top.zhoudl.nettystudy.protocol.serialize.Serializer;
import top.zhoudl.nettystudy.protocol.serialize.impl.JSONSerializationImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhoudongliang
 * @date: 2019/5/27 11:27
 * @description: java 对象封装成二进制数据包
 */
public class PacketCodec {
    /**
     * 魔数
     */
    private static final int MAGIC_NUMBER = 0x12345678;

    public static final PacketCodec INSTANCE = new PacketCodec();

    /**
     * 初始化 序列化算法 和 指令集
     */
    private final Map<Byte, Class<? extends Packet>> packetTypeMap;
    private final Map<Byte, Serializer> serializerMap;

    /**
     * 单例
     */
    private PacketCodec() {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(Command.LOGIN_REQUEST, LoginRequestPacket.class);
        packetTypeMap.put(Command.LOGIN_RESPONSE, LoginResponsePacket.class);
        packetTypeMap.put(Command.MESSAGE_REQUEST, MessageRequestPacket.class);
        packetTypeMap.put(Command.MESSAGE_RESPONSE, MessageResposePacket.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializationImpl();
        serializerMap.put(serializer.getSerializationAlgorithm(), serializer);
    }

    /**
     * 编码数据包
     * @param packet
     * @return
     */
    public ByteBuf encode (ByteBufAllocator byteBufAllocator, Packet packet) {

        // 1. 构建 ByteBuf 对象
        // ioBuffer() 方法会返回适配 io 读写相关的内存，
        // 它会尽可能创建一个直接内存，
        // 直接内存可以理解为不受 jvm 堆管理的内存空间，写到 IO 缓冲区的效果更高。
        ByteBuf byteBuf = byteBufAllocator.ioBuffer();
        // 2. 序列化 packet 对象
        byte[] bytes = Serializer.DEFAULT.serialization(packet);
        // 3. 开始编码
        // 魔数
        byteBuf.writeInt(MAGIC_NUMBER);
        // 版本号
        byteBuf.writeByte(packet.getVersion());
        // 序列化算法
        byteBuf.writeByte(Serializer.DEFAULT.getSerializationAlgorithm());
        // 指令
        byteBuf.writeByte(packet.getCommand());
        // 数据长度
        byteBuf.writeInt(bytes.length);
        // 数据
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }

    /**
     * 解码数据包
     * @param byteBuf
     * @return
     */
    public Packet decode(ByteBuf byteBuf) {
        // 跳过魔数
        byteBuf.skipBytes(4);
        // 跳过版本号
        byteBuf.skipBytes(1);
        // 序列化算法标识
        byte serializationAlgorithm = byteBuf.readByte();
        // 指令
        byte command = byteBuf.readByte();
        // 数据包长度
        int length = byteBuf.readInt();
        // 数据内容
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializationAlgorithm);

        if (requestType != null && serializer != null) {
            return serializer.deserialization(requestType, bytes);
        }
        return null;
    }

    private Serializer getSerializer(byte serializeAlgorithm) {
        return serializerMap.get(serializeAlgorithm);
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }


}
