> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：<https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521>

**指令数据**包分为 **指令**和**数据**

### 传统的 Socket IO 编程

1. 线程资源受限：同一时刻有大量的线程处于阻塞状态是非常严重的资源浪费，操作系统耗不起；
2. 线程切换效率低下：单机 CPU 核数固定，线程爆炸之后操作系统频繁进行线程切换，应用性能急剧下降；
3. IO 读写面向字节。

### NIO 编程

NIO 编程模型很好的解决了上述两个问题。

#### 线程资源受限问题

NIO 编程模型中，新来的一个连接不再创建一个新的线程，而是可以把这条连接直接绑定到某个固定的线程，然后这条连接所有的读写都由这个线程来负责。

IO 模型中，每个连接都会创建一个新线程，对应一个 while 循环，死循环的目的就是不断检测这条连接上是否有数据可以读，大多数情况下，同一时刻只有少量的连接有数据可读，因此，很多个 while 死循环都白白浪费掉了，因为读不出啥数据。

NIO 模型中，把这么多的 while 死循环变成了一个死循环，这个死循环由一个线程控制：

> 一条连接来了之后，现在不创建一个 while 死循环去监听是否有数据可读了，而是直接把这条连接注册到 selector 上，然后，通过检查这个 selector，就可以批量监测出有数据可读的连接，进而读取数据，下面我再举个非常简单的生活中的例子说明 IO 与 NIO 的区别。

#### 线程切换效率低下

NIO 模型中线程数量大大降低，线程切换效率大幅度提高了。

#### IO 读写面向字节流

IO 读写是面向字节流的，一次只能从流中读取一个或者多个字节，并且读完之后无法再次读取，需要自己缓存数据，而 NIO 的读写是面向 Buffer 的，可以随意读取里边的任何一个字节数据，也不需要自己缓存数据，我们只需要移动读写指针即可。



------------------------------------------------

> 
>
> JDK 的 NIO 犹如带刺的玫瑰，虽然美好，让人向往，但是使用不当会让你抓耳挠腮，痛不欲生，正因为如此，Netty 横空出世！

### Netty 是什么

Netty 封装了 JDK 的 NIO，让使用者用的更爽，不用再写一大段复杂的代码了。

> Netty 是一个异步事件驱动的网络应用框架，用于快速开发可维护的高性能服务器和客户端。

1. JDK 自带的 NIO 需要了解的概念田铎，编程复杂；

2. Netty 底层 IO 模型随意切换，只需要修改参数，Netty 可以直接从 NIO 模型变身为 IO 模型；

3. Netty 自带的拆包解包、异常检测等机制可以让使用者从 NIO 繁重的细节中脱离出来，程序员只需要关注业务逻辑；

4. Netty 解决了 JDK 的很多空轮询在内的 Bug；

5. Netty 底层对线程、selector 做了很多细小的优化，精心设计的 reactor 线程模型可以做到非常高效的并发处理；

6. 自带各种协议栈；

7. 社区活跃；

8. Netty 已经经历各大 RPC 框架、消息中间件、分布式通信中间件线上的广泛验证，健壮性无比强大。

   

----------



###  服务端启动流程分析

代码参考前篇中的 `NettyServer.java`

```java
public class NettyServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                    }
                });

        serverBootstrap.bind(8000);
    }
}
```

1. 创建两个 `NioEventLoopGroup` 线程组, `bossGroup` 表示监听端口，接受新连接的线程组，`workerGroup` 表示处理每一条连接的数据读写的线程组。可以简单理解为一个公司的运作模式：老板出去谈合作，签完合同拿到货之后丢给公司员工自己去处理，在这个例子中，`bossGroup` 就是老板，`workerGroup` 就是普通员工（对，哪个负责 996 的员工，手动狗头）；
2. 创建引导类 `ServerBootStrap`, 这个类负责引导我们进行服务端的启动工作；
3. `.group(bossGroup,workerGroup)` 给引导类配置第一步定义的两大线程组；
4. `channel(NioServerSocketChannel.class)` 负责指定线程模型为 NIO，还记得我们上一篇文章中说的使用 Netty 可以随意切换线程模型么，就是利用这个参数，如果此处要指定 IO 模型的话需要这样写 `channel(OioServerSocketChannel.class)` ,当然，除非某种特殊极端情况，我们是不会这么做的，Netty 的优势就在于 NIO 线程模型；
5. `.childHandler(new ChannelInitializer<NioSocketChannel>() ` 给引导类创建了一个 `ChannelInintializer`, 主要任务是 定义后续每条连接的数据读写以及业务处理逻辑，该方法有参数中有个 `NioSocketChannel`,可以理解成它是 Netty 对NIO 类型的连接的抽象。简单来理解，`NioServerSocketChannel` 和 `NioSocketChannel` 对应于 BIO 模型中的 `ServerSocket` 以及 `Socket` 两个概念。

以上可以说是最小化参数配置，后续会介绍更多参数配置。

简单总结一下：

> 我们要启动一个 Netty 服务端，必须要指定三个东西：
>
> 1. 线程模型；
> 2. IO 模型；
> 3. 具体读写处理逻辑
>
> 以上三者都指定好之后，绑定一个本地端口 `bind(8080)`便可以启动了。

#### 自动绑定递增端口

有的时候 Netty 启动的时候我们会发现端口被占用或者其他各种端口不可用原因导致绑定端口失败了，这时我们需要重新指定端口，然后再去重新启动程序。

但是作为程序员的我们，懒是我们的天性，

![img](file:///C:\Users\DELL\AppData\Local\Temp\SGPicFaceTpBq\31652\143F8013.png)

哪么如何让这个过程可以自动化执行呢？

改造一下上述程序，实现自动递增端口，直到绑定成功。

`serverBootstrap.bind(8080)` 这个方法是异步的，调用之后可以立即得到返回结果，返回值是 `ChannelFuture`, 我们可以给这个 `ChannelFuture`添加一个监听器 `GenericFutureListener`, 然后在`GenericFutureListener`的`operationComplete`方法里面，我们可以监听端口是否绑定成功。

```java
// 检测端口是否绑定成功
serverBootstrap.bind(8088).addListener(new GenericFutureListener<Future<? super Void>>() {
    public void operationComplete(Future<? super Void> future) {
        if (future.isSuccess()) {
            System.out.println("端口绑定成功!");
        } else {
            System.err.println("端口绑定失败!");
        }
    }
});

```

重构一下上述方法，将绑定逻辑抽取成一个单独方法，方便递归调用：

```java
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
```



--------------



### 客户端启动流程

客户端启动代码如下，不了解的可以参考上篇文章 [ Netty 从入门到实战（一）--- Netty 是什么？](https://blog.csdn.net/ZBylant/article/details/90408231)

```java
public class NettyClient {
    public static void main(String[] args) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                // 1.指定线程模型
                .group(workerGroup)
                // 2.指定线程模型为 NIO 模型
                .channel(NioSocketChannel.class)
                // 3.指定 IO 处理逻辑
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                    }
                });
        // 4.建立连接
        bootstrap.connect("127.0.0.1", 8080).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else {
                System.err.println("连接失败!");
            }

        });
    }
}
```

具体过程分析：

1. 和服务端启动类似，首先指定线程模型（参考上篇  [ Netty 从入门到实战（二）--- Netty服务端启动过程分析](https://blog.csdn.net/ZBylant/article/details/90512010)）；
2. 指定 IO 模型为 `NioSocketChannel`；
3. 给引导类指定 `handler`, 指定具体的业务处理逻辑；
4. 调用 `connect`方法进行连接，连接的时候第一个参数为 IP 或者域名，第二个参数为端口号，同样的，和 server 端的`bind`方法类似，`connect`方法也是异步的，同样返回一个 `Future`，意味着我们给他添加一个 `addListener`方法可以监听到连接是否成功。

#### 失败重连

很多时候我们都会碰到网络比价差的时候，这就会造成客户端的连接失败，很多应用程序都会实现一个尝试重新连接的功能逻辑，我们这个客户端同样需要这个功能：

```java
boostrap.connect("127.0.0.1",8080).addListener(future -> {
    if(future.isSuccess()){
        System.out.println("连接失败")
    } else {
        System.out.println("连接失败");
        // 重新连接逻辑
    }
})
```

具体分析一下：

重试连接的逻辑和连接的逻辑是一样的，所以结合软件编程中封装复用的思想，我们可以对其进行抽取，形成一个单独的方法，然后递归调用自身：

```java
// 1.0 版本
private static void connect(Bootstrap bootstrap, String host, int port) {
    bootstrap.connect(host, port).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("连接成功!");
        } else {
            System.err.println("连接失败，开始重连");
            connect(bootstrap, host, port);
        }
    });
}
```

**注意**

通常情况下，连接建立失败不会立即重新连接，而是会通过一个指数退避的方式，根据一定是的时间间隔，比如 2 的次幂来简历连接，到达一定次数之后就放弃连接，所以，我们对上述带代码做优化：

```java
// 2.0 版本
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
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                bootstrap.config()
                        .group()
                        .schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
            }
        });
    }
// 调用方式
// MAX_RETRY 最大重试次数
connect(bootstrap,"127.0.0.1",8070,MAX_RETRY);
```

以上逻辑比较简单，三个分支，分别不同的执行条件。

上面代码中，需要注意的地方是：我定时任务用的是 `bootstrap.config.group.schedule()` ,大家感兴趣的可以单独看一下，`bootstrap.config（）` 的返回值是 `BootstrapConfig`, 这个是对 `Bootstrap` 配置参数的抽象，然后 `bootstrap.config().group()` 返回的是我们最开始配置的线程模型 `workerGroup`,调用 `workerGroup` 的 `schedule` 方法即可实现定时任务逻辑。



----------------------------



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
   * 获取 netty 对二进制数据的抽象 `ByteBuf`，上段代码中的 `ctx.alloc()`会获取到一个 `ByteBuf`的内存管理器，这个内存管理器的作用就是分配一个 `ByteBuf`；
   * 把获取的 二进制数据填充到 `ByteBuf`中；
   * 调用 `ctx.channel().writeAndFlush()`把数据写到服务端。

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

* 服务端向客户端回复消息
* 客户端读取服务端回复过来的消息并打印

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

* 客户端和服务端在启动阶段会自动调用数据读写处理逻辑；
* 写数据调用 `writeAndFlush()`方法；
* 客户端和服务端交互的二级制数据载体为 `ByteBuf`,`ByteBuf`通过连接的内存管理器创建，字节数据只有填充到 `ByteBuf`之后才能写到对端。



--------------



### 数据传输载体 ByteBuff 介绍

上一篇文章 [Netty从入门到实战 --- 客户端和服务端双向通信](https://blog.csdn.net/ZBylant/article/details/90575280) 中说到，只有把字节数据填充到 `ByteBuf`才能写到对端，哪么，`ByteBuf`到底是个什么东西？我们今天就来剖析一下。

#### ByteBuf 的结构

![ByteBuf 结构 图片来源于闪电侠掘金小册 Netty从入门到实战](https://user-gold-cdn.xitu.io/2018/8/5/1650817a1455afbb?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

闪电侠大佬画出来的这个图已经说明了一切，我们可以很容易的看出来，一个 `ByteBuf` 实际上就是一个字节容器：

* 容器里面的数据分三部分
  * 第一部分：已经丢弃的字节；
  * 第二部分：可读字节；
  * 第三部分：可写字节；
  * 最后还有一段虚线部分代表了 可扩容字节；
* 以上三段内容是根据两个指针进行划分的，一个是可读指针` readIndex`,还有一个是可写指针 `writeIndex`;
* 变量 `capacity` 代表了 `ByteBuf` 底层的内存总容量
*  `ByteBuf `每读取一个字节，`readIndex` 就会增加 1，直到 `readIndex == writeIndex` 的时候代表该 `ByteBuf` 不可读，也就是说，意味着 `ByteBuf` 中总共有 `writeBuf - readIndex `个字节可读；
*  `ByteBuf `每写一个字节，`writeIndex` 就会增加 1，直到增加到` writeIndex == capacity` 代表该 `ByteBuf `不可写；
* 参数 `maxCapacity` 代表该 `ByteBuf` 的最大容量，在写 `ByteBuf` 数据时，如果容量不足，`ByteBuf` 可以进行扩容，直到 `capacity == maxCapacity` ，一旦超过 `maxCapacity` 会报错。

#### 相关 API 介绍

##### 容量相关

> capacity()

表示 `ByteBuf` 底层占用了多少字节的内存（包括丢弃的字节、可读字节、可写字节），不同的底层实现机制有不同的计算方式。

> maxCapacity()

表示 `ByteBuf` 底层最大能够占用多少字节的内存，当向 `ByteBuf` 中写数据的时候，如果发现容量不足，则进行扩容，直到扩容到 `maxCapacity`，超过这个数，就抛异常。

> readableBytes() 与 isReadable()

`readableBytes() `表示 `ByteBuf `当前可读的字节数，它的值等于 `writerIndex-readerIndex`，如果两者相等，则不可读，`isReadable()` 方法返回 `false`。

> writableBytes()、 isWritable() 与 maxWritableBytes()

`writableBytes() `表示 `ByteBuf` 当前可写的字节数，它的值等于 `capacity-writerIndex`，如果两者相等，则表示不可写，`isWritable()` 返回 `false`，但是这个时候，并不代表不能往 `ByteBuf` 中写数据了， 如果发现往 `ByteBuf` 中写数据写不进去的话，Netty 会自动扩容 `ByteBuf`，直到扩容到底层的内存大小为` maxCapacity``，而 maxWritableBytes()` 就表示可写的最大字节数，它的值等于 `maxCapacity-writerIndex`

##### 读写指针相关的 API

> readerIndex() 与 readerIndex(int)

前者表示返回当前的读指针` readerIndex`, 后者表示设置读指针。

> writeIndex() 与 writeIndex(int)

前者表示返回当前的写指针 `writerIndex`, 后者表示设置写指针。

> markReaderIndex() 与 resetReaderIndex()

前者表示把当前的读指针保存起来，后者表示把当前的读指针恢复到之前保存的值，下面两段代码是等价的。

```java
// 片段 1
int readIndex = buffer.readIndex();
// ... 其他操作
buffer.readerIndex(readIndex);

// 片段 2
buffer.markReadIndex();
// ... 其他操作
buffer.resetReaderIndex();

/**
推荐大家使用 片段 2 这种写法：
不需要自己定义变量，无论 buffer 当做参数传递到哪里，调用 resetReadIndex() 都可以恢复到指点的状态，在解析自定义数据包的时候非常常见（后文会提及）
*/
```

> markWriterIndex() 与 resetWriterIndex()

这两个 API 的作用和 `markReaderIndex()` 与 `resetReaderIndex()`类似。

##### 读写 API

> writeBytes(byte[] src) 与 buffer.readBytes(byte[] dst)

`writeBytes()` 表示把字节数组 src 里面的数据全部写到 ByteBuf，而 `readBytes()` 指的是把 ByteBuf 里面的数据全部读取到 dst，这里 dst 字节数组的大小通常等于 `readableBytes()`，而 src 字节数组大小的长度通常小于等于 `writableBytes()`。

> writeByte(byte b) 与 buffer.readByte() 

`writeByte()` 表示往 ByteBuf 中写一个字节，而 `buffer.readByte() `表示从 ByteBuf 中读取一个字节，类似的 API 还有 `writeBoolean()`、`writeChar()`、`writeShort()`、`writeInt()`、`writeLong()`、`writeFloat()`、`writeDouble()` 与 `readBoolean()`、`readChar()`、`readShort()`、`readInt()`、`readLong()`、`readFloat()`、`readDouble()` 这里就不一一赘述了，相信读者应该很容易理解这些 API。

与读写 API 类似的 API 还有 `getBytes`、`getByte()` 与 `setBytes()`、`setByte() `系列，**唯一的区别就是 get/set 不会改变读写指针，而 read/write 会改变读写指针，这点在解析数据的时候千万要注意**。

> release() 与 retain()

由于 Netty 使用了**堆外内存**，而堆外内存是不被 JVM 直接管理的，也就是说申请到的内存无法被垃圾回收器直接回收，所以需要我们**手动回收**。

有点类似于 C 语言里面，申请到的内存必须手工释放，否则会造成内存泄漏。

> Netty 的 ByteBuf 是通过引用计数的方式管理的，如果一个 ByteBuf 没有地方被引用到，需要回收底层内存。
>
> 默认情况下，当创建完一个 ByteBuf，它的引用为 1，然后每次调用 retain() 方法， 它的引用就加一， release() 方法原理是将引用计数减一，减完之后如果发现引用计数为 0，则直接回收 ByteBuf 底层的内存。

>
> slice()、duplicate()、copy()

这三个方法通常情况会放到一起比较，这三者的返回值都是一个新的 ByteBuf 对象。

1. `slice()`方法从原始 ByteBuf 中截取一段，这段数据是从 readIndex 到 writeIndex;
2. `duplicate()`方法把整个 ByteBuf 都截取出来，包括所有的数据、指针信息；
3. `slice()` 和 `duplicate()` 方法比较：
   * 相同点：底层内存与引用计数与原始的 ByteBuf 共享，也就是说经过 `slice()` 和 `duplicate()`返回的 ByteBuf 调用 write 系列的所有方法都会影响到原始的 ByteBuf
   * 不同点：截取内容不一样
     *  `slice()`只截取从 readerIndex 到 writerIndex 之间的数据
     * `duplicate() `是把整个 ByteBuf 都与原始的 ByteBuf 共享
   * 这俩方法都不会拷贝数据，只是通过改变读写指针来改变读写行为，而 `copy()`方法会直接从原始的 ByteBuf 中拷贝所有的信息，包括读写指针以及底层对应的数据，因此，改变 `copy()` 返回的 ByteBuf 不会影响到原始的 ByteBuf。
4. `slice()` 和 `duplicate()` 不会改变 ByteBuf 的引用计数，所以原始的 ByteBuf 调用 `release()` 之后发现引用计数为零，就开始释放内存，调用这两个方法返回的 ByteBuf 也会被释放，这个时候如果再对它们进行读写，就会报错。
5. 以上三个方法均维护着自己的读写指针，与原始的 ByteBuf 的读写指针无关，相互之间不受影响。

> retainedSlice() 与 retainedDuplicate()

相信读者应该已经猜到这两个 API 的作用了，它们的作用是在截取内存片段的同时，增加内存的引用计数，分别与下面两段代码等价。

```java
// retainedSlice 等价于
slice().retain();

// retainedDuplicate() 等价于
duplicate().retain()
```

使用到 slice 和 duplicate 方法的时候，千万要理清内存共享，引用计数共享，读写指针不共享几个概念，下面举两个常见的易犯错的例子。

> retainedSlice() 与 retainedDuplicate()

相信读者应该已经猜到这两个 API 的作用了，它们的作用是在截取内存片段的同时，增加内存的引用计数，分别与下面两段代码等价。

```java
// retainedSlice 等价于
slice().retain();

// retainedDuplicate() 等价于
duplicate().retain()
```

**注意**：**使用到 slice 和 duplicate 方法的时候，千万要理清内存共享，引用计数共享，读写指针不共享几个概念**。

此处指出两个常见的易犯错误的例子：

* 多次释放
* 不释放造成内存泄漏

为了避免以上两个常见错误，我们需要牢记一点，哪就是 

> 在一个函数体里面，只要增加了引用计数（包括 ByteBuf 的创建和手动调用 retain() 方法），就必须调用 release() 方法。

#### 总结

* Netty 对二进制数据的抽象 ByteBuf 的结构，本质上他的原理就是：引用了一段内存，这段内存可以使堆外内存也可以是堆内内存，然后使用引用计数的方式来控制这段内存是否要被释放，使用读写指针来控制 ByteBuf 的读写；
* 要注意 get/set 方法不会改变读写指针，而 read/write 方法会改变读写指针；
* 多个 ByteBuf 可以引用同一段内存，通过引用计数来控制内存的释放，遵循谁 `retain()`谁 `release()`的原则。

