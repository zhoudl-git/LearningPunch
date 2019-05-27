> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

### 服务端启动流程分析

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