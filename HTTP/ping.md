![](https://user-gold-cdn.xitu.io/2018/12/16/167b5b66c6f4eef8?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

### 一、概览

对于ping命令，想必只要是程序员都知道吧？当我们检查网络情况的时候，最先使用的命令肯定是ping命令吧？一般我们用ping查看网络情况，主要是检查两个指标，第一个是看看是不是超时，第二个看看是不是延迟太高。如果超时那么肯定是网络有问题啦（禁ping情况除外），如果延迟太高，网络情况肯定也是很糟糕的。那么对于ping命令的原理，ping是如何检查网络的？大家之前有了解吗？接下来我们来跟着ping命令走一圈，看看ping是如何工作的。

### 二、环境准备和抓包

**2.1 环境准备**

1.抓包工具。我这里使用Wireshark。

2.我准备了两台电脑，进行ping的操作。 ip地址分别为：

A电脑：192.168.2.135

mac地址：98:22:EF:E8:A8:87

B电脑：192.168.2.179

MAC：90:A4:DE:C2:DF:FE

**2.2 抓包操作**

打开 Wireshark，选取指定的网卡进行抓包，进行ping操作，在A电脑上ping B电脑的ip

![图a](https://user-gold-cdn.xitu.io/2018/12/17/167bb39332710498?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

抓包情况如下：

![图b](https://user-gold-cdn.xitu.io/2018/12/23/167d90b140e40a97?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

这里先简单的介绍下Wireshark的控制面板，这个面板包含7个字段，分别是：

- NO.编号
- Time:包的时间戳
- Source:源地址
- Destination:目标地址
- Protocol:协议
- Length:包长度
- Info:数据包附加信息

### 三、深入解析

上图中抓包编号54-132显示的就是整个ping命令的过程，我们知道ping命令不是依托于TCP或者UDP这种传输层协议的，而是依托于ICMP协议实现的， 那么什么是ICMP协议呢？这里简单介绍下：

**3.1 ICMP协议的产生背景**

https://tools.ietf.org/html/rfc792 [[RFC792\]](https://link.juejin.im?target=https%3A%2F%2Ftools.ietf.org%2Fhtml%2Frfc792)中说明了ICMP产生的原因：由于互联网之间通讯会涉及很多网关和主机，为了能够报告数据错误，所以产生了ICMP协议。也就是说ICMP协议就是为了更高效的转发IP数据报和提高交付成功的机会。

**3.2 ICMP协议的数据格式**

![图c](https://user-gold-cdn.xitu.io/2018/12/23/167d931514269c88?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



 根据上图我们知道了ICMP协议头包含4个字节，头部主要用来说明类型和校验ICMP报文。下图是对应的类型和代码释义列表，我们后面分析抓包的时候会用到。 

![图d](https://user-gold-cdn.xitu.io/2018/12/23/167d932caaa22a15?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

 简单介绍完了ICMP，那么抓包过程中出现的ARP协议是什么呢？我们同样来简单解释下： 

**3.3 ARP协议**

我们知道，在一个局域网中，计算机通信实际上是依赖于MAC地址进行通信的，那么ARP（Address Resolution Protocol）的作用就是根据IP地址查找出对应ip地址的MAC地址。

**3.4 Ping过程解析**

了解了上面的基础概念后，我们来分析下抓包的数据。图b的流程如下：

- A电脑（192.168.2.135）发起ping请求，ping 192.168.2.179
- A电脑广播发起ARP请求，查询 192.168.2.179的MAC地址。
- B电脑应答ARP请求，向A电脑发起单向应答，告诉A电脑自己的MAC地址为90:A4:DE:C2:DF:FE
- 知道了MAC地址后，开始进行真正的ping请求，由于B电脑可以根据A电脑发送的请求知道源MAC地址，所有就可以根据源MAC地址进行响应了。

上面的请求过程我画成流程图比较直观一点：

![img](https://user-gold-cdn.xitu.io/2018/12/23/167da29bf3e6f6f5?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

观察仔细的朋友可能已经发现，Ping4次请求和响应结束后，还有一次B电脑对A电脑的ARP请求，这是为什么呢？这里我猜测应该是有2个原因：

1.由于ARP有缓存机制，为了防止ARP过期，结束后重新更新下ARP缓存，保证下次请求能去往正确的路径，如果ARP过期就会导致出现一次错误，从而影响测试准确性。

2.由于ping命令的响应时间是根据请求包和响应包的时间戳计算出来的，所以一次ARP过程也是会消耗时间。这里提前缓存最新的ARP结果就是节省了下次ping的arp时间。

为了验证我们的猜测，我再进行一次ping操作，抓包看看是不是和我们猜测的一样。此时，计算机里面已经有了ARP的缓存，我们执行ARP -a 看看缓存的arp列表：

![](https://user-gold-cdn.xitu.io/2018/12/23/167da336bf1e39f3?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

我们看看第二次ping的抓包

![](https://user-gold-cdn.xitu.io/2018/12/23/167da33f10c36f3e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

我们看到上图中在真正ping之前并没有进行一次ARP请求，这也就是说，直接拿了缓存中的arp来执行了，另外当B计算机进行响应之前还是进行了一次ARP请求，它还是要确认下之前的ARP缓存是否为正确的。然后结束ping操作之后，同样在发一次ARP请求，更新下自己的ARP缓存。这里和我们的猜想基本一致。

弄懂了ping的流程之后我们来解析下之前解释的ICMP数据结果是否和抓包的一致。 我们来点击一个ping request看看ICMP协议详情

![img](https://user-gold-cdn.xitu.io/2018/12/23/167da3d218ddff6c?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

 图中红框内就行ICMP协议的详情了，这里的Type=8,code=0,校验是正确。我们对比图d,就知道了这是一个请求报文。我们再点击Response frame:57，这里说明响应报文在序号57。详情如下：

![img](https://user-gold-cdn.xitu.io/2018/12/23/167da402a34a5f70?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

上图的响应报文，Type=0,code=0。这里知道就是响应报文了，然后最后就是根据请求和响应的时间戳计算出来的响应延迟。3379.764 ms-3376.890 ms=2.874 ms.

### 四、总结

我们分析了一次完整的ping请求过程，ping命令是依托于ICMP协议的，ICMP协议的存在就是为了更高效的转发IP数据报和提高交付成功的机会。ping命令除了依托于ICMP，在局域网下还要借助于ARP协议，ARP协议能根据IP地址查出计算机MAC地址。ARP是有缓存的，为了保证ARP的准确性，计算机会更新ARP缓存。

> 作者：木木匠
链接：https://juejin.im/post/5c15ec0f6fb9a049ec6af8b2

