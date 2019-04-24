### Arthas是什么？

在此借用官方的一段话:

> 当你遇到以下类似问题而束手无策时，`Arthas`可以帮助你解决：
>
> 1. 这个类从哪个 jar 包加载的？为什么会报各种类相关的 Exception？
> 2. 我改的代码为什么没有执行到？难道是我没 commit？分支搞错了？
> 3. 遇到问题无法在线上 debug，难道只能通过加日志再重新发布吗？
> 4. 线上遇到某个用户的数据处理有问题，但线上同样无法 debug，线下无法重现！
> 5. 是否有一个全局视角来查看系统的运行状况？
> 6. 有什么办法可以监控到JVM的实时运行状态？
>
> `Arthas`支持JDK 6+，采用命令行交互模式，同时提供丰富的 `Tab` 自动补全功能，进一步方便进行问题的定位和诊断。

接下来，话不多说，开始动手实操了

### 安装

#### windos平台

下载地址：[maven-central v3.0.5](https://img.shields.io/maven-central/v/com.taobao.arthas/arthas-packaging.svg?style=flat-square) 

https://img.shields.io/maven-central/v/com.taobao.arthas/arthas-packaging.svg?style=flat-square

解压后目录如下：

![Arthas](http://pkon92vqd.bkt.clouddn.com/Arthas%E8%A7%A3%E5%8E%8B%E5%90%8E%E7%9A%84%E6%96%87%E4%BB%B6%E7%9B%AE%E5%BD%95.png)

在此我们着重关注 `arthas-boot.jar`这个文件，我们可以使用它来启动 `Arthas`

#### linux 平台

可以直接使用 以下 命令

```shell
wget https://alibaba.github.io/arthas/arthas-boot.jar
```

如果从 github 下载速度比较慢的话推荐  使用 码云的 镜像：

```shell
wget https://arthas.gitee.io/arthas-boot.jar
```

>除了以上安装方式，还有两种安装方式：
>
>* 使用 as.sh 的方式
>* 全量安装方式
>
>具体安装方式请参考官网，此处不再赘述。

### 快速使用

先启动官方提供的测试 demo 

```
java -jar arthas-demo.jar
```

![Arthas demo 启动成功](http://pkon92vqd.bkt.clouddn.com/arthas-demo%E5%90%AF%E5%8A%A8.png)

直接使用以下命令进行启动：

```shell
java -jar arthas-boot.jar
```

> **注意**：在 windows 平台下，直接启动会报找不到 tools.jar 的错，从源码来看是因为此处寻找 tools.jar 没有从本地配置的 JAVA_HOME 去获取，而是先去获取的 JVM 的 tools.jar, 因此需要我们手动指定以下 JAVA_HOME 的目录，不知道这个算是 bug ，还是有意为之，期待官方以后的解决方案。
>
> 所以 windows 平台下的命令变成了 
>
> ```shell
> java -Djava.home="D:\jdk\jre" -jar arthas-boot.jar
> ```
>
> `-Djava.home` 用于指定本地 JAVA_HOME 目录

![Arthas 启动成功](http://pkon92vqd.bkt.clouddn.com/Arthas%E5%90%AF%E5%8A%A8%E6%88%90%E5%8A%9F.png)

### 常用命令介绍

#### dashboard

`dashboard`展示当前进程的信息，按`ctrl+c` 中断执行

```shell
12     AsyncAppender-Work system        5     WAITIN 0     0:0    false true
5      Attach Listener    system        5     RUNNAB 0     0:0    false true
3      Finalizer          system        8     WAITIN 0     0:0    false true
2      Reference Handler  system        10    WAITIN 0     0:0    false true
4      Signal Dispatcher  system        9     RUNNAB 0     0:0    false true
21     Timer-for-arthas-d system        10    RUNNAB 0     0:0    false true
20     as-command-execute system        10    TIMED_ 0     0:0    false true
14     job-timeout        system        5     TIMED_ 0     0:0    false true
1      main               main          5     TIMED_ 0     0:34   false false
15     nioEventLoopGroup- system        10    RUNNAB 0     0:0    false false
Memory           used  total max  usage GC
heap             31M   243M       0.86%                     2
ps_eden_space    15M   63M        1.17% gc.ps_scavenge.time 14
                 10M   10M   10M        (ms)
ps_old_gen       5M    169M       0.18% gc.ps_marksweep.cou 0
nonheap          21M   21M   -1         nt
Runtime
os.name             Windows 10
os.version          10.0
java.version        1.8.0_144
java.home           D:\jre
systemload.average  -1.00
ID     NAME               GROUP         PRIOR STATE  %CPU  TIME   INTER DAEMON

```

#### jad

`jad`使用该命令 反编译 class 文件

```shell
$ jad demo.MathGame
jad demo.MathGame

ClassLoader:
+-sun.misc.Launcher$AppClassLoader@55f96302
  +-sun.misc.Launcher$ExtClassLoader@74a14482

Location:
/E:/study/tools-study/Arthas/arthas-packaging-3.0.5-bin/arthas-demo.jar

/*
 * Decompiled with CFR 0_132.
 */
package demo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MathGame {
    private static Random random = new Random();
    private int illegalArgumentCount = 0;

    public static void main(String[] args) throws InterruptedException {
        MathGame game = new MathGame();
        do {
            game.run();
            TimeUnit.SECONDS.sleep(1L);
        } while (true);
    }
... ...
```

#### thread

`thread -n -1 | grep 'main('`

```shell
$ thread -n -1 | grep 'main('
thread -n -1 | grep 'main('
    at demo.MathGame.main(MathGame.java:17)
```

#### watch

方法执行数据观测,这个命令可以方便的观察到指定方法的调用情况。能观察到的范围为：`返回值`、`抛出异常`、`入参`，通过编写 OGNL 表达式进行对应变量的查看

详细参数说明请参考 官方文档： https://alibaba.github.io/arthas/watch.html

> ###### 参数说明
>
> watch 的参数比较多，主要是因为它能在 4 个不同的场景观察对象
>
> | 参数名称            | 参数说明                                   |
> | ------------------- | ------------------------------------------ |
> | *class-pattern*     | 类名表达式匹配                             |
> | *method-pattern*    | 方法名表达式匹配                           |
> | *express*           | 观察表达式                                 |
> | *condition-express* | 条件表达式                                 |
> | [b]                 | 在**方法调用之前**观察                     |
> | [e]                 | 在**方法异常之后**观察                     |
> | [s]                 | 在**方法返回之后**观察                     |
> | [f]                 | 在**方法结束之后**(正常返回和异常返回)观察 |
> | [E]                 | 开启正则表达式匹配，默认为通配符匹配       |
> | [x:]                | 指定输出结果的属性遍历深度，默认为 1       |
>
> 这里重点要说明的是观察表达式，观察表达式的构成主要由 ognl 表达式组成，所以你可以这样写`"{params,returnObj}"`，只要是一个合法的 ognl 表达式，都能被正常支持。



###### 观察方法出参和返回值

```shell
$ watch demo.MathGame primeFactors "{params,returnObj}" -x 2
Press Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 44 ms.
ts=2018-12-03 19:16:51; [cost=1.280502ms] result=@ArrayList[
    @Object[][
        @Integer[535629513],
    ],
    @ArrayList[
        @Integer[3],
        @Integer[19],
        @Integer[191],
        @Integer[49199],
    ],
]
```

###### 观察方法入参

```
$ watch demo.MathGame primeFactors "{params,returnObj}" -x 2 -b
Press Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 50 ms.
ts=2018-12-03 19:23:23; [cost=0.0353ms] result=@ArrayList[
    @Object[][
        @Integer[-1077465243],
    ],
    null,
]
```

###### 观察异常信息

```shell
$ watch demo.MathGame primeFactors "{params[0],throwExp}" -e -x 2
Press Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 62 ms.
ts=2018-12-03 19:38:00; [cost=1.414993ms] result=@ArrayList[
    @Integer[-1120397038],
    java.lang.IllegalArgumentException: number is: -1120397038, need >= 2
    at demo.MathGame.primeFactors(MathGame.java:46)
    at demo.MathGame.run(MathGame.java:24)
    at demo.MathGame.main(MathGame.java:16)
,
]
```

###### 按照耗时进行过滤

```shell
$ watch demo.MathGame primeFactors '{params, returnObj}' '#cost>200' -x 2
Press Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 66 ms.
ts=2018-12-03 19:40:28; [cost=2112.168897ms] result=@ArrayList[
    @Object[][
        @Integer[2141897465],
    ],
    @ArrayList[
        @Integer[5],
        @Integer[428379493],
    ],
]
```

### 退出arthas

如果只是退出当前的连接，可以用`quit`或者`exit`命令。Attach到目标进程上的arthas还会继续运行，端口会保持开放，下次连接时可以直接连接上。

如果想完全退出arthas，可以执行`shutdown`命令。