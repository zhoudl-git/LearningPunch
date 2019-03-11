### 01 闲谈

基础学课程 远不止几个参数而已！

p6 原理

p7 以上 GC调优

### 02 大纲

* JVM 介绍与类加载器

* JVM 内存模型介绍

### 03 课程目标

* 了解JVM内存模型以及每个分区详解

* 熟悉运行时数据区，特别是堆内存结构和特点

* 熟悉GC三种手机方法的原理和特点

* 熟练使用GC调优工具，快速诊断线上问题

* 生产环境CPU负载升高怎么处理

* 生产环境给应用分配多少线程合适

* JVM字节码是个什么东西

### 04 先来看个栗子

```
java -version 
jps
jinfo -flags pid 查看 jvm 的信息
-Xms
-Xmx
jmap -heap pid 查看堆信息 比如OOM
名词解释：
MaxNewSize 新生代
MinHeapDeltaBytes 
OldSize 老年代
```

### 05  看一个图

JVM JRE JDK

