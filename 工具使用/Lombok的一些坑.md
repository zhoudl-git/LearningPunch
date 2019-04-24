原文链接：https://dwz.cn/jUke5dxm

作者：Cyberspace_TechNode

#### Lombok背景介绍

官方介绍如下：

```
Project Lombok makes java a spicier language by adding 'handlers' that know how to build and compile simple, boilerplate-free, not-quite-java code.
```

简单来讲，就是在项目中使用 Lombok 可以减少很多重复代码的书写。比如说 getter/setter/toString 等方法的编写。

#### 1. 一些杂七杂八的问题

##### 1.1 额外的环境配置

作为 IDE 插件 + jar 包，需要对 IDE 进行一系列的配置。目前在 idea 中配置还算简单，几年前在 eclipse 下也配置过，会复杂不少。

##### 1.2 传染性

一般来说，对外打的 jar 包最好尽可能地减少三方包依赖，这样可以加快编译速度，也能减少版本冲突。一旦在resource 包里用了 lombok，别人想看源码也不得不装插件。

而这种不在对外 jar 包中使用 lombok 仅仅是约定俗成，当某一天 lombok 第一次被引入这个 jar 包时，新的感染者无法避免。

##### 1.3 降低代码可读性

定位方法调用时，对于自动生成的代码，getter/setter 还好说，找到成员变量后 find usages，再根据上下文区分是哪种；equals() 这种，想找就只能写段测试代码再去 find usages 了。

目前主流 ide 基本都支持自动生成 getter/setter 代码，和 lombok 注解相比不过一次键入还是一次快捷键的区别，实际减轻的工作量十分微小。

#### 2. @EqualsAndHashCode和equals()

##### 2.1 原理

当这个注解设置`callSuper=true`时，会调用父类的 equlas() 方法，对应编译后 class 文件代码片段如下：

```java
public boolean equals(Object o) {
    if (o == this) {
        return true;
    } else if (!(o instanceof BaseVO)) {
        return false;
    } else {
        BaseVO other = (BaseVO)o;
        if (!other.canEqual(this)) {
            return false;
        } else if (!super.equals(o)) {
            return false;
        } else { 
            // 各项属性比较
        }
    }
}
```

如果一个类的父类是 Object（java 中默认没有继承关系的类父类都是 Object），那么这里会调用 Object 的equals() 方法，如下

```java
public boolean equals(Object obj) {
    return (this == obj);
}
```

##### 2.2 问题

对于父类是 Object 且使用了`@EqualsAndHashCode(callSuper = true)` 注解的类，这个类由 lombok 生成的equals() 方法只有在两个对象是同一个对象时，才会返回 true，否则总为 false，无论它们的属性是否相同。这个行为在大部分时间是不符合预期的，equals() 失去了其意义。即使我们期望 equals() 是这样工作的，那么其余的属性比较代码便是累赘，会大幅度降低代码的分支覆盖率。以一个近 6000 行代码的业务系统举例，是否修复该问题并编写对应测试用例，可以使整体的 jacoco 分支覆盖率提高 10%~15%。

相反地，由于这个注解在 jacoco 下只算一行代码，未覆盖行数倒不会太多。

##### 2.3 解决

有几种解决方法可以参考：

- 不使用该注解。大部分 pojo 我们是不会调用 equals 进行比较的，实际用到时再重写即可。
- 去掉`callSuper = true`。如果父类是 Object，推荐使用。
- 重写父类的 equals() 方法，确保父类不会调用或使用类似实现的 Ojbect 的 equals()。

##### 2.4 其他

`@data`注解包含`@EqualsAndHashCode`注解，由于不调用父类 equals()，避免了 Object.equals() 的坑，但可能带来另一个坑。详见`@data章节`。

#### 3. @data

##### 3.1 从一个坑出来掉到另一个大坑

上文提到 @EqualsAndHashCode(callSuper = true) 注解的坑，那么 `@data` 是否可以避免呢？很不幸的是，这里也有个坑。
由于 `@data` 实际上就是用的 `@EqualsAndHashCode`，没有调用父类的 equals()，当我们需要比较父类属性时，是无法比较的。示例如下：

```java
@Data
public class ABO {
    private int a;

}

@Data
public class BBO extends ABO {

    private int b;

    public static void main(String[] args) {

        BBO bbo1 = new BBO();
        BBO bbo2 = new BBO();

        bbo1.setA(1);
        bbo2.setA(2);

        bbo1.setB(1);
        bbo2.setB(1);

        System.out.print(bbo1.equals(bbo2)); // true
    }
}
```

很显然，两个子类忽略了父类属性比较。这并不是因为父类的属性对于子类是不可见——即使把父类 private 属性改成 protected，结果也是一样——而是因为 lombok 自动生成的equals()只比较子类特有的属性。

##### 3.2 解决方法

- 用了 `@data` 就不要有继承关系，类似 kotlin 的做法，具体探讨见下一节
- 自己重写 equals()，lombok 不会对显式重写的方法进行生成
- 显式使用`@EqualsAndHashCode(callSuper = true)`。lombok 会以显式指定的为准。

##### 3.3 关于@data和data

在了解了 `@data` 的行为后，会发现它和 kotlin 语言中的 data 修饰符有点像：都会自动生成一些方法，并且在继承上也有问题——前者一旦有继承关系就会踩坑，而后者修饰的类是final的，不允许继承。kotlin 为什么要这样做，二者有没有什么联系呢？在一篇流传较广的文章([抛弃 Java 改用 Kotlin 的六个月后，我后悔了(译文)](https://blog.csdn.net/csdnnews/article/details/80746096))中，对于data修饰符，提到：

对于Liskov（里氏替换）原则，可以简单概括为：

> 一个对象在其出现的任何地方，都可以用子类实例做替换，并且不会导致程序的错误。换句话说，当子类可以在任意地方替换基类且软件功能不受影响时，这种继承关系的建模才是合理的。

根据上一章的讨论，equals() 的实现实际上是受业务场景影响的，无论是否使用父类的属性做比较都是有可能的。但是 kotlin 无法决定 equals() 默认的行为，不使用父类属性就会违反了这个原则，使用父类属性有可能落入调用Object.equals() 的陷阱，进入了两难的境地。

kotlin 的开发者回避了这个问题，不使用父类属性并且禁止继承即可。只是 kotlin 的使用者就会发现自己定义的data 对象没法继承，不得不删掉这个关键字手写其对应的方法。

回过头来再看 `@data` ，它并没有避免这些坑，只是把更多的选择权交给开发者决定，是另一种做法。

#### 4. 后记

其他 lombok 注解实际使用较少，整体阅读了 [官方文档](https://projectlombok.org/features/all)暂时没有发现其他问题，遇到以后继续更新。


  https://blog.csdn.net/csdnnews/article/details/80746096