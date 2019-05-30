> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

### Netty 实现客户端登录

#### 登录流程

![图片来源于 闪电侠 掘金小册 Netty 入门与实战：仿写微信 IM 即时通讯系统](https://user-gold-cdn.xitu.io/2018/8/14/16535d7424e02d3a?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

闪电侠在小册中描述的这幅图很形象，完整概括了 客户端连上服务端 之后的全过程，图很清晰，大家看图就行，我便不再赘述了。

### 逻辑处理器

接下来我们根据登陆流程图来做具体实现：

回顾前面小节提到的客户端和服务端的启动流程，我们需要定义两个处理器。

服务端处理器`ServerHandler.java`, 客户端处理器 `CLientHandler.java`。

定义好了处理器，接下来我们就用 Handler 来编写我们的具体处理逻辑。

### 客户端发送登录请求

#### 客户端处理登录请求

前边小节的学习我们知道在客户端连接上服务端之后，Netty 会回调到 `ClientHandler.java` 的 `channelActive()` 方法，所以我们可以把具体的登录逻辑写到这个方法中：

> ClientHandler.java

```java
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import top.zhoudl.nettystudy.protocol.command.LoginRequestPacket;
import top.zhoudl.nettystudy.protocol.command.LoginResponsePacket;
import top.zhoudl.nettystudy.protocol.command.Packet;
import top.zhoudl.nettystudy.protocol.command.PacketCodec;

import java.util.Date;

/**
 * @author: zhoudongliang
 * @date: 2019/5/29 15:55
 * @description:
 */
public class ClientHandler  extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + ": 客户端开始登录");

        // 创建登录对象
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        loginRequestPacket.setUserId(1);
        loginRequestPacket.setUserName("程序猿杂货铺");
        loginRequestPacket.setPassword("pwd");

        // 编码
        ByteBuf buffer = PacketCodec.INSTANCE.encode(ctx.alloc(), loginRequestPacket);

        // 写数据
        ctx.channel().writeAndFlush(buffer);
    }

}
```

再对之前封装的消息对象 `PacketCodec.java` 进行一点改变，改成单例模式的，代码如下：

```java
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

```

#### 服务端处理登录请求

> ServerHandler.java

```java
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import top.zhoudl.nettystudy.protocol.command.LoginRequestPacket;
import top.zhoudl.nettystudy.protocol.command.LoginResponsePacket;
import top.zhoudl.nettystudy.protocol.command.Packet;
import top.zhoudl.nettystudy.protocol.command.PacketCodec;

import java.util.Date;

/**
 * @author: zhoudongliang
 * @date: 2019/5/29 15:55
 * @description:
 */
public class ServerHandler  extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println(new Date() + ": 客户端开始登录……");
        ByteBuf requestByteBuf = (ByteBuf) msg;

        Packet packet = PacketCodec.INSTANCE.decode(requestByteBuf);

        if (packet instanceof LoginRequestPacket) {
            
            if (valid(loginRequestPacket)) {
                loginResponsePacket.setSuccess(true);
                System.out.println(new Date() + ": 登录成功!");
            } else {
                loginResponsePacket.setReason("账号密码校验失败");
                loginResponsePacket.setSuccess(false);
                System.out.println(new Date() + ": 登录失败!");
            }
            // 登录响应
            ByteBuf responseByteBuf = PacketCodec.INSTANCE.encode(ctx.alloc(), loginResponsePacket);
            ctx.channel().writeAndFlush(responseByteBuf);
        }
    }

    /**
     * 校验用户名密码正确性
     * @param loginRequestPacket
     * @return
     */
    private boolean valid(LoginRequestPacket loginRequestPacket) {
        // 目前先假设所有登录请求都是成功的，所以直接返回 true 
        return true;
    }
}
```

### 服务端发送登录响应

构造一个登录响应 `LoginResponsePacket.java`

> LoginResponsePacket.java

```java
import lombok.Data;

/**
 * @author: zhoudongliang
 * @date: 2019/5/29 16:10
 * @description:
 */
@Data
public class LoginResponsePacket extends Packet {
    /**
     * 响应是否成功
     */
    private boolean success;

    /**
     * 响应失败原因
     */
    private String reason;

    @Override
    public Byte getCommand() {
        return Command.LOGIN_RESPONSE;
    }
}
```

> ServerHandler.java

```java
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import top.zhoudl.nettystudy.protocol.command.LoginRequestPacket;
import top.zhoudl.nettystudy.protocol.command.LoginResponsePacket;
import top.zhoudl.nettystudy.protocol.command.Packet;
import top.zhoudl.nettystudy.protocol.command.PacketCodec;

import java.util.Date;

/**
 * @author: zhoudongliang
 * @date: 2019/5/29 15:55
 * @description:
 */
public class ServerHandler  extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println(new Date() + ": 客户端开始登录……");
        ByteBuf requestByteBuf = (ByteBuf) msg;

        Packet packet = PacketCodec.INSTANCE.decode(requestByteBuf);

        if (packet instanceof LoginRequestPacket) {
            // 登录流程
            LoginRequestPacket loginRequestPacket = (LoginRequestPacket) packet;

            LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
            loginResponsePacket.setVersion(packet.getVersion());
            if (valid(loginRequestPacket)) {
                loginResponsePacket.setSuccess(true);
                System.out.println(new Date() + ": 登录成功!");
            } else {
                loginResponsePacket.setReason("账号密码校验失败");
                loginResponsePacket.setSuccess(false);
                System.out.println(new Date() + ": 登录失败!");
            }
            // 登录响应
            ByteBuf responseByteBuf = PacketCodec.INSTANCE.encode(ctx.alloc(), loginResponsePacket);
            ctx.channel().writeAndFlush(responseByteBuf);
        }
    }

    /**
     * 校验用户名密码正确性
     * @param loginRequestPacket
     * @return
     */
    private boolean valid(LoginRequestPacket loginRequestPacket) {
        return true;
    }
}
```

#### 客户端处理登录响应

> ClientHandler.java

在 `ClientHandler.java`中增加如下方法：

```java

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        Packet packet = PacketCodec.INSTANCE.decode(byteBuf);

        if (packet instanceof LoginResponsePacket) {
            LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;

            if (loginResponsePacket.isSuccess()) {
                System.out.println(new Date() + ": 客户端登录成功");
            } else {
                System.out.println(new Date() + ": 客户端登录失败，原因：" + loginResponsePacket.getReason());
            }
        }

    }
```

### 测试

客户端输出结果

```
连接成功!
Wed May 29 16:23:10 CST 2019: 客户端开始登录
```

服务端输出结果

```java
端口[8080]绑定成功!
Wed May 29 16:23:10 CST 2019: 客户端开始登录……
Wed May 29 16:23:10 CST 2019: 登录成功!
```

### 总结

这节代码比较简单，其实就是在上一节 [客户端与服务端通信协议设计](https://blog.csdn.net/ZBylant/article/details/90604088) 的代码基础上做了小部分改变，所以看这节代码如果看不明白的话，大家要去上一小节内容温故一下。

关于 Netty 这部分内容，笔者也是边学边记的过程，一直在看闪电侠的掘金小册，再次感谢闪电侠大佬！

在本节内容中，闪电侠在文章最后留下了一个思考题，我觉得很有意思,问题是这样的：

> 客户端登录成功或者失败之后，如果把成功或者失败的标识绑定在客户端的连接上？服务端又是如何高效避免客户端重新登录？





P.S 提示一下，可以使用 hannel 的 attr() 方法来加一些特殊的标记，达到存放这个登录的标示的目的！

--------------------

> 欢迎关注微信公众号 【程序猿杂货铺】，获取更多干货内容！

> 完整代码 参考Github [Netty 从入门到实战](https://github.com/Bylant/LearningPunch)