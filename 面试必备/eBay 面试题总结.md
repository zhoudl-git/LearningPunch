## eBay

#### switch是否能作用在String上？

* Java 5 之前，switch(expr) 中，expr 只能是` byte\short\char\int`；

* Java 5 之后，引入了枚举类型；

* Java 7 开始，引入了字符串；

#### Integer num1 = 127, Integer num2 = 127, 判断 num1 == num2？

Integer 类有缓存，-127 ~ 128 之间会直接从常量池获取，超过这个范围会在堆中创建新对象

#### try-with-resource 语法

Java 7 之后提供的优雅关闭资源的方式

```java
public static void main(String[] args) {
    try (FileInputStream inputStream = new FileInputStream(new File("test"))) {
        System.out.println(inputStream.read());
    } catch (IOException e) {
        throw new RuntimeException(e.getMessage(), e);
    }
}
```

将外部资源的句柄对象的创建放在 try 关键字后面的括号中，当这个 try-catch 代码块执行完毕后，Java 会确保外部资源的 close 方法被调用。

##### 实现原理

并不是 JVM 虚拟机的新增功能，只是 JDK 实现了一个语法糖，反编译后会发现依旧是之前的写法，只不过 Java 通过语法糖帮我们简化了代码。

另外还有一点需要注意，反编译之后代码中会有一种**异常抑制**的特殊处理 `addSuppressed()`，当对外部资源进行处理（例如读或写）时，如果遭遇了异常，且在随后的关闭外部资源过程中，又遭遇了异常，那么你 catch 到的将会是对外部资源进行处理时遭遇的异常，关闭资源时遭遇的异常将被“抑制”但不是丢弃，通过异常的 getSuppressed 方法，可以提取出被抑制的异常。

##### 总结

1. 当一个外部资源的句柄对象实现了 AutoCloseable 接口，JDK7 中便可以利用 try-with-resource 语法更优雅的关闭资源，消除板式代码。

2. try-with-resource 时，如果对外部资源的处理和对外部资源的关闭均遭遇了异常，“关闭异常”将被抑制，“处理异常”将被抛出，但“关闭异常”并没有丢失，而是存放在“处理异常”的被抑制的异常列表中。

#### Java 8 的日期类

##### 旧版存在的问题

* 非线程安全 ：java.util.Date 非线程安全，所有的日期类都是可变的；
* 设计很差 ：java.util.Date java.sql.Date 用于格式化的却又在 java.text 中；
* 时区处理很麻烦 ：没有提供国际化，没有时区支持；

新版 java.time 包涵盖了所有处理日期，时间，时区，时刻，过程与时钟的操作。

* Local 本地 ：简化了日期时间的处理，没有时区的问题
* Zoned 时区 ：通过制定的时区处理日期时间

###### 本地化日期时间 API 

LocalDate/LocalTime/LocalDateTime 

java.time 包

LocalDate 类

LocalTime

LocalDateTime

#### 谈谈对 java8 中 parallelStream（并行流）

Stream具有平行处理能力，处理的过程会分而治之，也就是将一个大任务切分成多个小任务。

parallelStream 其实就是一个并行执行的流，它通过默认的 ForkJoinPool，可能提高你的多线程任务的速度。

ForkJoin 框架是从 jdk7 中新特性,它同 ThreadPoolExecutor 一样，也实现了 Executor 和 ExecutorService 接口。它使用了一个无限队列来保存需要执行的任务，而线程的数量则是通过构造函数传入，如果没有向构造函数中传入希望的线程数量，那么当前计算机可用的 CPU 数量会被设置为线程数量作为默认值。

#### 谈谈对于 hash 的理解

#### concurrentHashMap 为什么并发效率高？

#### 线程有哪些状态？线程的启动方法是什么？直接调用线程的run方法呢？线程可以设置优先级，优先级高的一定有限执行吗？

#### Synchronized 和 ReentrantLock 的区别与使用场景？

#### 阐述一下悲观锁、乐观锁。

#### 怎么理解 spring  的 ioc 和 aop 的，aop 的使用场景？spring 中体现了什么设计模式？

#### Redis 的分布式锁用了什么指令。

#### 消息队列中怎么解决消费端消息丢失的问题？

#### 多数据源的事务控制（分布式事务）。

#### JVM 的了解和使用。

