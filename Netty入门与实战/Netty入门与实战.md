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
    serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
        public void operationComplete(Future<? super Void> future) {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
                bind(serverBootstrap, port + 1);
            }
        }
    });
}
```

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
    bootstrap.connect(host,port).addListener(future -> {
        if(future.isSuccess()) {
            System.out.println("连接成功！");
        } else {
            System.out.println("连接失败，重连中...");
            connect(bootstrap,host,port);
        }
    });
}
```

**注意**

通常情况下，连接建立失败不会立即重新连接，而是会通过一个指数退避的方式，根据一定是的时间间隔，比如 2 的次幂来简历连接，到达一定次数之后就放弃连接，所以，我们对上述带代码做优化：

```java
// 2.0 版本
private static void connect(Bootstrap bootstrap,String host,int port,int retry) {
    bootstrap.connect(host,port).addListener(future -> {
        if(future.isSuccess()) {
            System.out.println("连接成功")；
        } else if(retry == 0){
            System.out.println("重试次数已用完");
        } else {
            // 第几次重连
            int count = (MAX_RETRY - retry) + 1;
            // 本次重连的间隔
            int delay = 1 << count;
            System.out.println("第 " + count + "次连接失败，连接时间：" + new Date())；
                bootstrap.config().group().schedule() -> connect(bootstrap,host,port,retry - 1),delay,TimeUnit.SECONDS);
        }
    })
}
// 调用方式
// MAX_RETRY 最大重试次数
bootstrap.connect("127.0.0.1",8070,MAX_RETRY);
```

以上逻辑比较简单，三个分支，分别不同的执行条件。

上面代码中，需要注意的地方是：我定时任务用的是 `bootstrap.config.group.schedule()` ,大家感兴趣的可以单独看一下，`bootstrap.config（）` 的返回值是 `BootstrapConfig`, 这个是对 `Bootstrap` 配置参数的抽象，然后 `bootstrap.config().group()` 返回的是我们最开始配置的线程模型 `workerGroup`,调用 `workerGroup` 的 `schedule` 方法即可实现定时任务逻辑。

### 客户端和服务端双向通信

聊天是一个双向通信的过程