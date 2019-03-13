# 学Guava发现：不可变特性与防御性编程

### 一、面试常谈：String 类与不可变特性

问：String 类是可变的吗？

答：emm……由于String类的底层是 final 关键字修饰，因此它是不可变的。

问：它被设计为不可变的好处有哪些呢？

答：

- 节约内存

  > 大家都知道，编程的时候，String 类是大量被使用的（*试着用 VisualVm 等工具分析堆，你会发现永远char[] 类型是占用空间最多的。巧了，String 类的底层实现也正是 char[]*）。

  > 如果像普通对象那样，每次使用都 new 一个，恐怕你设置的 JVM 堆大小得慎重考虑一下了。

  > 因此出现了一个叫做常量池的东西，比如`String a="abc"`，`String b="abc"`，那么 a 和 b 都指向常量池的`"abc"`这个地址。这样，多个变量，可以共用一个常量池地址，节约了内存。

- 线程安全

  > 常说实现线程安全的方法之一就是使用`final`关键字将变量修改为常量，那么为什么不可变的常量是线程安全的呢？

  > 很简单，比如多线程并发修改同一变量，如果不加同步进行控制，必然会出现数据不一致问题。但是由于String 类是不可变的，根本就不支持你修改，那怎么可能出现数据不一致问题呢？（感觉像是在扯淡，o(∩_∩)o 哈哈！）

- 数据安全

  > 这里的数据安全，就和下文说道的防御性编程有关系了。

  > 假设String类可变：

  ```java
  String name1 = "张三";
  String name2 = name1;
  user.setName(name1);
  name2 = "李四";
  System.out.println(user.getName());
  
  输出：李四
  ```

  > what？这位用户明明名字叫`张三`，咋个无端变成`李四`了？

- 提高缓存效率

  > 大家都知道`HashMap.put(key,value)`，需要对key进行hashcode运算。

  > hashcode 是 String 类型。因为 String 的不可变特性，就不需要担心 hashcode 值被修改，可以缓存起来多次使用，减少 hashcode 计算次数。

### 二、进阶梳理：不可变特性与防御性编程

有一个**Period 类**：

```java
public final class Period {

    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
        if (start.compareTo(end) > 0)
            throw new IllegalArgumentException(start + " after " + end);
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
```

乍一看，这个类似乎是不可变的（即类中的数据不会发生变化）。而事实上，真的是这样吗？

```java
Date start = new Date();
Date end = new Date();

Period p = new Period(start, end);

end.setYear(78); 

System.out.println(p.getEnd().toLocalString());

输出：1978-3-2 18:38:40
```

以上代码和刚刚的那个“张三、李四”的例子很像。**在类实例的外部，直接修改无关变量值，最后导致类实例内部的数据也变化了。**

这种情况往往不易被程序员在编码时所发现，从而由于数据的变化导致业务 bug。

因此，想要把 Period 类设计为一个不可变的类，有这么几种方案：

- `Instant`、`LocalDateTime`或`ZonedDateTime`来代替Date

  > 使用从Java 8开始，解决此问题的显而易见的方法是使用`Instant`、`LocalDateTime`或`ZonedDateTime`来代替Date。因为Instant和其他java.time包下的类是不可变的。**Date已过时，不应再在新代码中使用**。

- Period类重设计

```java
public Period(Date start, Date end) {
	
	//防御性拷贝：构造一个新Date对象，这样，这个内部的start变量和外面的那个start变量将没有任何联系
    this.start = new Date(start.getTime());
    this.end   = new Date(end.getTime());

    if (this.start.compareTo(this.end) > 0)
      throw new IllegalArgumentException(this.start + " after " + this.end);
}
```

上面提到了一个名词：“防御性拷贝”，很确切。除了构造新 Date 对象，还有深克隆的方式，但是此处不推荐使用克隆。至于为什么？由于篇幅有限，大家可自行百度！

那么，这样就实现了 Period 类不可变了吗？

并没有！由于该类内部的私有数据还提供了 getter 方法，因此仍然可能通过 getter 方法修改该类的内部数据。

因此，我们还需要：

```java
 public Date getStart() {
        return new Date(start.getTime());
    }

    public Date getEnd() {
        return new Date(end.getTime());
    }
```

这个有点像数据库中的`视图`了，可以给你看，但你不能修改源！

------

最后总结一下，防御性编程到底是什么呢？

**防御性编程是一种比较泛化的概念，是一种细致、谨慎的编程习惯。**

我们在写代码的时候，需要时刻考虑到：`代码是否正确？` `代码是否正确？` `代码是否正确？`

例如：

- 你可以利用不可变特性、构造时拷贝对象等方法来确保一个类的不可变
- 很多时候，考虑使用防御性拷贝，避免直接在原始实例上进行操作
- 接收参数时考虑参数的是否非空等
- 是否引发性能问题、死锁问题
- ……

### 三、JAVA 设计：我感受到的防御性编程

#### 1、String、Integer 等的不可变特性

原因上面已经说明了！

#### 2、Arrays.asList 返回仅可查看的“视图”

Arrays.asList() 返回一个ArrayList内部类，没有`add()`、`remove()`、`无法改变长度`等，这样设计的初衷是什么？为什么不直接返回可变长的 ArrayList(new ArrayList())？

和我们刚刚的重写 getter 方法类似，用于保证对象安全不可改变特性！

举个例子，就是你有一个数组，怎么设计一个方法：保证既可以遍历，又不能修改呢？

返回一个继承了`List接口`的轻量级`“视图”`不失为一个好的设计方式。而直接返回数组则是不安全的选择。

#### 3、不可变集合的各种实现

为什么需要不可变集合？

不可变对象有很多优点，包括：

- 当对象被不可信的库调用时，不可变形式是安全的；
- 不可变对象被多个线程调用时，不存在竞态条件问题
- 不可变集合不需要考虑变化，因此可以节省时间和空间。所有不可变的集合都比它们的可变形式有更好的内存利用率（分析和测试细节）；
- 不可变对象因为有固定不变，可以作为常量来安全使用。
- 创建对象的不可变拷贝是一项很好的防御性编程技巧。

如果你没有修改某个集合的需求，或者希望某个集合保持不变时，把它防御性地拷贝到不可变集合是个很好的实践。

#### JDK 的实现

JDK 的 Collections 类提供以下不可变集合，用于开发者的一些不可变需求：

![img](https://user-gold-cdn.xitu.io/2019/3/7/1695641ff088e678?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



#### Guava 的实现

同时，Guava亦提供以下不可变集合：

![img](https://user-gold-cdn.xitu.io/2019/3/7/1695641ff28ac18b?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)