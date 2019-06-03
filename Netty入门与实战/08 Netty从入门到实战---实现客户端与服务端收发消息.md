> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

## 实现客户端和服务端收发消息

> 在控制台输入一条消息之后按回车，校验完客户端的登录状态之后，把消息发送到服务端，服务端收到消息之后打印并且向客户端发送一条消息，客户端收到之后打印。

### 收发消息对象

先定义一下指令类型

> Command.java

```java
public interface Command {

    /**
     * 登录指令
     */
    Byte LOGIN_REQUEST = 1;

    /**
     * 登录响应指令
     */
    Byte LOGIN_RESPONSE = 2;

    /**
     * 响应消息指令
     */
    Byte MESSAGE_REQUEST = 3;

    /**
     * 请求消息指令
     */
    Byte MESSAGE_RESPONSE = 4;


}

```



定义一下客户端与服务端的消息对象

> MessageRequestPacket.java

```java
@Data
public class MessageRequestPacket extends Packet{

    private String message;

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_REQUEST;
    }

}

```

> MessageResponsePacket.java

```java
@Data
public class MessageResposePacket extends Packet{

    private String message;

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_RESPONSE;
    }
}
```

### 判断客户端是否登录成功

上篇文章 [Netty从入门到实战（七）--- 实现客户端登录](https://blog.csdn.net/ZBylant/article/details/90675266) 给大家留了个思考题：

> 客户端登录成功或者失败之后，如果把成功或者失败的标识绑定在客户端的连接上？服务端又是如何高效避免客户端重新登录？

这篇文章就来回答一下这个问题的答案及具体做法。我们通过给 Channel 绑定属性的方式来实现对于成功登录的客户端进行标记，然后其他时候直接获取这个属性进行判断即可得到客户端状态。

先定义如下登录成功标志位：

> Attribute.java

```java
public interface Attributes {
    AttributeKey<Boolean> LOGIN = AttributeKey.newInstance("login");
}
```

在客户端登录成功后，增加绑定成功的标志位

> ClientHandler.java 完整代码见文末 github 地址

```java
if (loginResponsePacket.isSuccess()) {
    // 标识该客户端已经登录
    LoginUtil.markAsLogin(ctx.channel());

    System.out.println(new Date() + ": 客户端登录成功");
} else {
    System.out.println(new Date() + ": 客户端登录失败，原因：" + loginResponsePacket.getReason());
}
```

> LoginUtil.java

```java
public class LoginUtil {

    public static void markAsLogin(Channel channel) {
        channel.attr(Attributes.LOGIN).set(true);
    }

    /**
     * 如果有标志位，不管标志位的值是什么，都表示已经成功登录过
     * @param channel
     * @return
     */
    public static boolean hasLogin(Channel channel) {
        Attribute<Boolean> loginAttr = channel.attr(Attributes.LOGIN);
        return loginAttr.get() != null;
    }
}

```

### 控制台输入消息并发送给服务端

> NettyClient.java

```java
private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
    bootstrap.connect(host, port).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("连接成功!");

            Channel channel = ((ChannelFuture) future).channel();
            // 开启控制台线程
            startConsoleThread(channel);
			// ...
        } 
    });
}
```

```java
/**
     * 开启控制台线程
     * @param channel
     */
private static void startConsoleThread(Channel channel) {

    new Thread(() -> {
        while (!Thread.interrupted()) {
            // 判断是否登录 登录状态下直接发送消息
            if (LoginUtil.hasLogin(channel)) {
                System.out.println("输入消息发送至服务端: ");
                // 获取控制台输入消息
                Scanner sc = new Scanner(System.in);
                String line = sc.nextLine();
				// 获取到的控制台输入封装成我们定义好的可供传输的 消息对象
                MessageRequestPacket packet = new MessageRequestPacket();
                packet.setMessage(line);
                 // 编码成 ByteBuf
                ByteBuf byteBuf = PacketCodec.INSTANCE.encode(channel.alloc(), packet);
                // 发型消息
                channel.writeAndFlush(byteBuf);
            }
        }
    }).start();
}
```

### 服务端收发消息处理

> ServerHandler.java

```java
@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf requestByteBuf = (ByteBuf) msg;

        Packet packet = PacketCodec.INSTANCE.decode(requestByteBuf);
		// ...
        if (packet instanceof MessageRequestPacket) {
            // 处理消息
            MessageRequestPacket messageRequestPacket = ((MessageRequestPacket) packet);

            System.out.println(new Date() + ": 收到客户端消息: " + messageRequestPacket.getMessage());
			// 封装消息对象
            MessageResposePacket messageResponsePacket = new MessageResposePacket();
            messageResponsePacket.setMessage("服务端回复【" + messageRequestPacket.getMessage() + "】");
            ByteBuf responseByteBuf = PacketCodec.INSTANCE.encode(ctx.alloc(), messageResponsePacket);
            ctx.channel().writeAndFlush(responseByteBuf);
        }
    }
```

### 客户端收消息处理

> ClientHandler.java

```java
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf byteBuf = (ByteBuf) msg;

    Packet packet = PacketCodeC.INSTANCE.decode(byteBuf);

    if (packet instanceof LoginResponsePacket) {
        // 登录逻辑...
    } else if (packet instanceof MessageResponsePacket) {
        MessageResponsePacket messageResponsePacket = (MessageResponsePacket) packet;
        System.out.println(new Date() + ": 收到服务端的消息: " + messageResponsePacket.getMessage());
    }
}
```

### 测试

> 客户端

![服务端](assets/服务端.png)

> 服务端

![服务端](assets/服务端.png)

