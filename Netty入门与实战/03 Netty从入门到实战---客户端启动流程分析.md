> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

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

