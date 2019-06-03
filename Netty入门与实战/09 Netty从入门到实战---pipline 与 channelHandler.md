> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

通过上篇文章 [Netty从入门到实战（七）--- 实现客户端与服务端收发消息](https://blog.csdn.net/ZBylant/article/details/90754627) 我们会发现代码中充斥了大量的 `if else ` 代码块，哪如何避免 `else ` 泛滥 ？

我们先回忆一下客户端和服务端的数据处理流程：

* 数据传输
* 编码（decode）
* `if else ` `登录` `消息收发`
* 解码（encode）
* 数据传输

以上逻辑过程我们都是写到一个类中的，客户端对应 `ClientHandler.java`, 服务端对应 `ServerHandler.java`，这就造成一个问题：我们一旦要做功能扩展，哪就只能去这个类里面改改改，久而久之，这个类就变得杂乱不堪，十分臃肿！

此外，每次数据传输之前我们都需要手动调用 ByteBuf 对数据进行处理，以上场景我们可以使用模块化处理，不同的逻辑放置到单独的类来处理，最后将这些逻辑串联起来，形成一个完整的逻辑处理链。

基于此，我们今天要学习的 `pipeline` 和 `channelHandler` 正好是用来解决这个问题的：

> pipeline 和 channelHandler 通过责任链设计模式来组织代码逻辑，并且能够支持逻辑的动态添加和删除，Netty 能够支持各种协议的扩展，靠的就是 pipeline 和 channelHandler 。

### pipeline 与 channelHandler 的构成

在 Netty 中 ，一条连接对应一个 `Channel`，这条 `Channel` 所有的处理逻辑都在 `ChannelPipeline` 的对象里面，`ChannelPipeline` 是一个双向链表结构，它和 `Channel` 是一对一的关系。

