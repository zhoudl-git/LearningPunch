> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

### 客户端和服务端双向通信

今天开始学习如何利用 Netty 来实现服务端和客户端的双向通信，不熟悉服务端和客户端启动流程的同学参考前面文章 [Netty从入门到实战-Netty 服务端启动过程分分析](https://blog.csdn.net/ZBylant/article/details/90512010) 以及 [Netty从入门到实战-Netty客户端启动过程分析](https://blog.csdn.net/ZBylant/article/details/90518161) ，熟悉了以上两个过程之后，本篇文章内容理解起来将会显得水到渠成。

> 实现一个功能：
>
> 客户端连接服务端，连接成功后，给服务端写一段数据，服务端接受数据后打印消息并且给客户端回复对应数据。

### 客户端发送数据给服务端

在 [Netty从入门到实战-Netty 服务端启动过程分分析](https://blog.csdn.net/ZBylant/article/details/90512010) 这篇文章中我们提到过，客户端的数据读写逻辑是通过如下代码来指定的：

```java
bootstrap.handler(new ChannelInitializer<SocketChannel> () {
    @Override
    public void initChannel(SocketChannel ch) {
        // 指定数据读写逻辑
    }
})
```

所以，我们来写一个对应的逻辑处理器：

```java
bootstrap.handler(new ChannelInitializer<ScoketChannel>() {
    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipline().addLast(new FirstClientHandler());
    }
})
```

> FirstClientHandler.java

```java
public class FirstServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + " -> 客户端开始写数据");
        // 获取 ByteBuf 内容
        ByteBuf buffer = getByteBuf(ctx);
        ctx.channel().writeAndFlush(buffer);
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        byte[] bytes = "你好，程序猿杂货铺".getBytes();
        buffer.writeBytes(bytes);
        return buffer;
    }

}
```

此处需要注意的是：

1. 我们定义的逻辑处理器需要继承 `ChannelInboundHandlerAdapter`,然后覆盖其中的 `channelActive()`方法，这个方法在连接简历成功之后会自动被调用，所以我们写数据的逻辑就卸载这个方法之中；
2. 写数据的逻辑分两步：
   - 获取 netty 对二进制数据的抽象 `ByteBuf`，上段代码中的 `ctx.alloc()`会获取到一个 `ByteBuf`的内存管理器，这个内存管理器的作用就是分配一个 `ByteBuf`；
   - 把获取的 二进制数据填充到 `ByteBuf`中；
   - 调用 `ctx.channel().writeAndFlush()`把数据写到服务端。

**留一个问题给大家**：

传统的 Socket 编程和 Netty 编程在数据传输类型上有什么不同？

### 服务端读取客户端数据

经过上面对客户端发送数据的过程讲解，相信大家举一反三，能很轻松的理解服务端的数据处理逻辑。

同理，在  [Netty从入门到实战-Netty客户端启动过程分析](https://blog.csdn.net/ZBylant/article/details/90518161)  这一篇文章中我们说了，服务端的处理逻辑是由以下代码来完成的：

```java
serverBootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
    @Override
    protected void initChannel(NioSocketChannel ch) {
        // 具体数据读写逻辑
    }
})
```

对比客户端发送数据的逻辑，此处我们肯定需要自己建立一个逻辑处理器。

```java
serverBootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
    @Override
    protected void initChannel(NioSocketChannel ch) {
        // 具体数据读写逻辑
        ch.pipline().addLast(new FirstServerHandlet());
    }
})
```

> FirstServerHandler.java

```java
public class FirstServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg) {
        ByteBuf byteBuf = (ByteBuf)msg;
        System.out.println(new Date() + "服务端读取到数据：" + byteBuf.toString(Charset.forName("utf-8"))) ;
    }
}
```

服务端处理器同样需要继承 `ChannelIUnboundHandlerAdapter`,与客户端的区别在于此处覆盖的方法不同，因为是读数据，从而覆盖的是`channelRead()`方法，这个方法在接收到客户端数据之后会自动被回调。

**此处再留一个问题**：

为什么在方法 `channelRead()`中我们需要把 `msg` 强转成 `ByteBuf`，为什么 netty 在设计的时候不直接把 `msg` 定义成 `ButeBuf`呢？

#### 完整代码

> NettyClient.java

```java
public class NettyClient {

    /**
     * 最大重试次数
     */
    public static final int MAX_RETRY = 5;
    /**
     * 端口号
     */
    public static final int PORT = 8080;
    /**
     * 地址
     */
    public static final String HOST = "127.0.0.1";

  public static void main(String[] args) {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 处理读写逻辑
                        socketChannel.pipeline().addLast(new FirstClientHandler());
                    }
                });

        connect(bootstrap, HOST, PORT, MAX_RETRY);
    }

    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else if (retry == 0) {
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第 " + order + " 次重连……");
                bootstrap.config()
                        .group()
                        .schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
            }
        });
    }

}

```

> NettyServer.java

```java
public class NettyServer {

    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new FirstServerHandler());
                    }
                });
        // 绑定端口
        bind(serverBootstrap,8080);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
                bind(serverBootstrap, port + 1);
            }
        });
    }

}
```

> 运行结果

```
NettyServer.java 

端口[8080]绑定成功!
Sun May 26 12:06:54 CST 2019: 服务端读到数据 -> 你好，程序猿杂货铺

NettyClient.java

Sun May 26 12:06:54 CST 2019: 客户端开始写数据
连接成功!
```

通过运行测试发现，这个收发逻辑没有问题，接下来我们再继续拓展新功能，在现有程序基础上增加：

- 服务端向客户端回复消息
- 客户端读取服务端回复过来的消息并打印

对 `FirstServerHandler.java`做修改，增加回复消息的逻辑代码，代码如下：

```java
@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 接受数据
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(new Date() + ": 服务端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));
        // 回复数据到客户端
        System.out.println(new Date() + ": 服务端写出数据");
        ByteBuf out = getByteBuf(ctx);
        ctx.channel().writeAndFlush(out);
    }
```

同样地，给客户端代码增加读取消息的逻辑，修改 `FirstClientHandler.java`代码,增加如下代码段：

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf byteBuf = (ByteBuf) msg;
    System.out.println(new Date() + ": 客户端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));
}
```

重新运行代码，测试执行结果：

```
NettyServer.java

端口[8080]绑定成功!
Sun May 26 13:21:14 CST 2019: 服务端读到数据 -> 你好，程序猿杂货铺
Sun May 26 13:21:14 CST 2019: 服务端写出数据

NettyClient.java

连接成功!
Sun May 26 13:21:14 CST 2019: 客户端开始写数据
Sun May 26 13:21:14 CST 2019: 客户端读到数据 -> 你好，我是程序猿杂货铺，公众号同名哦，欢迎关注，我们一起学习!

```

我们刚开始提到的功能已经全部实现了，以下做个简单总结

#### 总结

- 客户端和服务端在启动阶段会自动调用数据读写处理逻辑；
- 写数据调用 `writeAndFlush()`方法；
- 客户端和服务端交互的二级制数据载体为 `ByteBuf`,`ByteBuf`通过连接的内存管理器创建，字节数据只有填充到 `ByteBuf`之后才能写到对端。

> 完整代码在 github 上 [使用 Netty 实现服务端和客户端双向通信](https://github.com/Bylant/LearningPunch/tree/master/Netty%E5%85%A5%E9%97%A8%E4%B8%8E%E5%AE%9E%E6%88%98/netty-study/src/main/java/top/zhoudl/nettystudy/communication)

