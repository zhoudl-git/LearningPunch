相信目前大多数情况下，不管是开源框架或是平时我们工作编码中都离不开一种框架，它就是日志框架。因此本篇文章就简单了解一下我们常用日志框架的区别。

### Commons Logging

common-logging 是 apache 提供的一个通用的日志接口，
在 common-logging 中，有一个 Simple logger 的简单实现，但是它功能很弱，所以使用 common-logging，通常都是配合着 log4j 来使用；

Commons Logging 定义了一个自己的接口 org.apache.commons.logging.Log，以屏蔽不同日志框架的 API 差异，这里用到了 Adapter Pattern（适配器模式）。

### SLF4J

Simple Logging Facade for Java（SLF4J）用作各种日志框架（例如 java.util.logging，logback，log4j）的简单外观或抽象，允许最终用户在部署时插入所需的日志框架。

要切换日志框架，只需替换类路径上的 slf4j 绑定。 例如，要从 java.util.logging 切换到 log4j，只需将 slf4j-jdk14-1.8.0-beta2.jar 替换为 slf4j-log4j12-1.8.0-beta2.jar。

SLF4J 不依赖于任何特殊的类装载机制。 实际上，每个 SLF4J 绑定在编译时都是硬连线的，以使用一个且只有一个特定的日志记录框架。 例如，slf4j-log4j12-1.8.0-beta2.jar 绑定在编译时绑定以使用 log4j。 在您的代码中，除了slf4j-api-1.8.0-beta2.jar 之外，您只需将您选择的一个且只有一个绑定放到相应的类路径位置。 注意不要在类路径上放置多个绑定。

以下是slf4j 绑定其它日志组件的图解说明。

![](https://img-blog.csdnimg.cn/20181216142102250.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTA2NDcwMzU=,size_16,color_FFFFFF,t_70)

因此，slf4j 就是众多日志接口的集合，他不负责具体的日志实现，只在编译时负责寻找合适的日志系统进行绑定。具体有哪些接口，全部都定义在 slf4j-api 中。查看 slf4j-api 源码就可以发现，里面除了 `public final class LoggerFactory` 类之外，都是接口定义。

因此，slf4j-api 本质就是一个接口定义。总之，Slf4j 更好的兼容了各种具体日志实现的框架，如图：

![](https://img-blog.csdnimg.cn/20181216143003494.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTA2NDcwMzU=,size_16,color_FFFFFF,t_70)

### Log4j

Apache Log4j 是一个非常古老的日志框架，并且是多年来最受欢迎的日志框架。 它引入了现代日志框架仍在使用的基本概念，如分层日志级别和记录器。

2015 年 8 月 5 日，该项目管理委员会宣布 Log4j 1.x 已达到使用寿命。 建议用户使用 Log4j 1 升级到 Apache Log4j 2。

### Log4j2

Apache Log4j 2是对 Log4j 的升级，它比其前身 Log4j 1.x 提供了重大改进，并提供了 Logback 中可用的许多改进，同时修复了 Logback 架构中的一些固有问题。

与 Logback 一样，Log4j2 提供对 SLF4J 的支持，自动重新加载日志配置，并支持高级过滤选项。 除了这些功能外，它还允许基于 lambda 表达式对日志语句进行延迟评估，为低延迟系统提供异步记录器，并提供无垃圾模式以避免由垃圾收集器操作引起的任何延迟。

所有这些功能使 Log4j2 成为这三个日志框架中最先进和最快的。

### Logback

logback 是由 log4j 创始人设计的又一个开源日志组件，作为流行的 log4j 项目的后续版本，从而替代 log4j。

Logback 的体系结构足够通用，以便在不同情况下应用。 目前，logback 分为三个模块：logback-core，logback-classic和logback-access。

* logback-core：模块为其他两个模块的基础。

* logback-classic：模块可以被看做是log4j的改进版本。此外，logback-classic 本身实现了 SLF4J API，因此可以在 logback 和其他日志框架（如 log4j 或 java.util.logging（JUL））之间来回切换。

* logback-access：模块与 Servlet 容器（如 Tomcat 和 Jetty）集成，以提供 HTTP 访问日志功能。

### 总结

我建议直接选择 SLF4J 而不是 Log4j，commons logging，logback 或 java.util.logging。

* 在开源库或内部库中使用 SLF4J，将使其独立于任何特定的日志记录实现，这意味着无需为多个库管理多个日志记录配置，您的客户端将会很需要这一点；
* SLF4J 提供了基于占位符的日志记录，通过删除检查（isDebugEnabled（），isInfoEnabled（）等）来提高代码的可读性；
* 另外，临时字符串数量越少意味着垃圾收集器的工作量就越少，这意味着应用程序的吞吐量和性能会更好。



这些优势只是冰山一角，当您开始使用 SL4J 深入了解它时，您将看到更多的优点。 

我强烈建议，Java 中的任何新代码开发都应该使用 SLF4J 来记录日志。



-----------

作者：IT码客 
来源：CSDN 
原文：https://blog.csdn.net/u010647035/article/details/85037206 
版权声明：本文为博主原创文章，转载请附上博文链接！