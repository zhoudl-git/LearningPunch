> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

### 数据传输载体 ByteBuff 介绍

上一篇文章 [Netty从入门到实战 --- 客户端和服务端双向通信](https://blog.csdn.net/ZBylant/article/details/90575280) 中说到，只有把字节数据填充到 `ByteBuf`才能写到对端，哪么，`ByteBuf`到底是个什么东西？我们今天就来剖析一下。

#### ByteBuf 的结构

![ByteBuf 结构 图片来源于闪电侠掘金小册 Netty从入门到实战](https://user-gold-cdn.xitu.io/2018/8/5/1650817a1455afbb?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

闪电侠大佬画出来的这个图已经说明了一切，我们可以很容易的看出来，一个 `ByteBuf` 实际上就是一个字节容器：

- 容器里面的数据分三部分
  - 第一部分：已经丢弃的字节；
  - 第二部分：可读字节；
  - 第三部分：可写字节；
  - 最后还有一段虚线部分代表了 可扩容字节；
- 以上三段内容是根据两个指针进行划分的，一个是可读指针` readIndex`,还有一个是可写指针 `writeIndex`;
- 变量 `capacity` 代表了 `ByteBuf` 底层的内存总容量
- `ByteBuf `每读取一个字节，`readIndex` 就会增加 1，直到 `readIndex == writeIndex` 的时候代表该 `ByteBuf` 不可读，也就是说，意味着 `ByteBuf` 中总共有 `writeBuf - readIndex `个字节可读；
- `ByteBuf `每写一个字节，`writeIndex` 就会增加 1，直到增加到` writeIndex == capacity` 代表该 `ByteBuf `不可写；
- 参数 `maxCapacity` 代表该 `ByteBuf` 的最大容量，在写 `ByteBuf` 数据时，如果容量不足，`ByteBuf` 可以进行扩容，直到 `capacity == maxCapacity` ，一旦超过 `maxCapacity` 会报错。

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

> slice()、duplicate()、copy()

这三个方法通常情况会放到一起比较，这三者的返回值都是一个新的 ByteBuf 对象。

1. `slice()`方法从原始 ByteBuf 中截取一段，这段数据是从 readIndex 到 writeIndex;
2. `duplicate()`方法把整个 ByteBuf 都截取出来，包括所有的数据、指针信息；
3. `slice()` 和 `duplicate()` 方法比较：
   - 相同点：底层内存与引用计数与原始的 ByteBuf 共享，也就是说经过 `slice()` 和 `duplicate()`返回的 ByteBuf 调用 write 系列的所有方法都会影响到原始的 ByteBuf
   - 不同点：截取内容不一样
     - `slice()`只截取从 readerIndex 到 writerIndex 之间的数据
     - `duplicate() `是把整个 ByteBuf 都与原始的 ByteBuf 共享
   - 这俩方法都不会拷贝数据，只是通过改变读写指针来改变读写行为，而 `copy()`方法会直接从原始的 ByteBuf 中拷贝所有的信息，包括读写指针以及底层对应的数据，因此，改变 `copy()` 返回的 ByteBuf 不会影响到原始的 ByteBuf。
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

- 多次释放
- 不释放造成内存泄漏

为了避免以上两个常见错误，我们需要牢记一点，哪就是 

> 在一个函数体里面，只要增加了引用计数（包括 ByteBuf 的创建和手动调用 retain() 方法），就必须调用 release() 方法。

#### 总结

- Netty 对二进制数据的抽象 ByteBuf 的结构，本质上他的原理就是：引用了一段内存，这段内存可以使堆外内存也可以是堆内内存，然后使用引用计数的方式来控制这段内存是否要被释放，使用读写指针来控制 ByteBuf 的读写；
- 要注意 get/set 方法不会改变读写指针，而 read/write 方法会改变读写指针；
- 多个 ByteBuf 可以引用同一段内存，通过引用计数来控制内存的释放，遵循谁 `retain()`谁 `release()`的原则。

