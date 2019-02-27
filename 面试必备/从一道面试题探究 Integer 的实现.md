记得有次面试，面试官问我：

>  如何写一个方法交换两个 Integer 类型的值？

当时心里一惊，这是把我当小白了呀！交换两个数的值还不容易么，最简单的直接搞一个中间变量，然后就可以交换了... ...
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112224700.jpg)

面试官随即拿出一张雪白雪白的 A4 纸

> 工具用多了，有没有体验过白纸写代码？来吧，开始你的表演，小伙子。

此时稍微有点心虚，但还是要装腔作势，把自己想象成大佬才行。

有的人可能会问，你不是说很简单么，还心虚个啥？写过代码的都知道，工具写代码是有自动补全提示的，这白板写代码纯粹就是考察你对代码的熟练度，其实相当考验代码功底。

于是乎，提起笔，奋笔疾书，唰唰唰不到两分钟，我就写完了。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112248937.gif)

代码如下：

```java
public static void main(String[] args) {
    Integer a = 1, b = 2;
    System.out.println("交换前：a = " + a + ", b = " + b);
    swap(a,b);
    System.out.println("交换后：a = " + a + ", b = " + b);
}

public static void swap(Integer i, Integer j) {
    int temp = i;
    i = j;
    j = temp;
}
```

当我胸有成竹的把纸递过去的时候，我仿佛看见面试官嘴角哪不经意间的微笑。

这一笑不要紧，要紧的是一个大男人对着我笑干嘛? 

难道我的代码感动到他了？

明人不说暗话，这明显不可能。

难道是他要对我... ...

想到此处，我不禁赶紧回忆了下来时的路，怎么样可以快速冲出去... ...

喂，醒醒，想啥呢

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112310576.jpg)

面试官瞄了一眼代码之后开始发问呢。

> 你确定你这段代码真的可以交换两个 Integer 的值吗？（竟然在 Integer 上加了重音）

我的天呐，难道有问题，多年面试经验告诉我，面试重音提问要不就是在故意混淆，要不就是在善意提醒你，看能不能挖掘出点其他技术深度出来。

所以根据面试官的意思肯定是使用这段代码不能交换呢，哪么不能交换的原因在哪里？
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112323564.jpg)

首先，想了下，要交换两个变量的值，利用中间变量这个思路是不会错的。既然思路没错，哪就要往具体实现上想，问题出在哪里。

##### 第一个知识点：**值传递和引用传递**

我们都知道，Java 中有两种参数传递

* 值传递

  方法调用时，实际参数把它的值传递给对应的形式参数，方法执行中形式参数值的改变不影响实际参数的值。

* 引用传递

  也称为传地址。方法调用时，实际参数的引用(地址，而不是参数的值)被传递给方法中相对应的形式参数，在方法执行中，对形式参数的操作实际上就是对实际参数的操作，方法执行中形式参数值的改变将会影响实际参数的值。

简单总结一下就是：

![img](https://images2015.cnblogs.com/blog/701142/201706/701142-20170613200934978-745949138.png)

也就是说 对象类型（地址空间）的变量存在于堆中，其引用存在于栈中。

至于为什么这么设计：主要还是为了考虑访问效率和提升代码性能上考虑的。

难道问题出在这个地方？

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112339991.jpg)

可是 Integer 不就是 **引用类型**？

为什么不能改变呢？

难道 Integer 的实现有什么特殊之处？



你别说，还真是 Integer 有他自己的独特之处。

##### 第二个知识点：Integer 在源码实现上存在这么一个属性

```java
/**
     * The value of the {@code Integer}.
     *
     * @serial
     */
private final int value;
```

这个属性也是表示这个 Integer 实际的值，但是他是 private final 的，Integer 的 API 也没有提供给外部任何可以修改它的值接口，也就是说这个值改变不了。

简单理解就是上面的 swap 方法其实真实交换的是 两个形参 i 和 j 的值，而没有去改变 a 和 b 的值

画个图简单理解一下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112438142.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1pCeWxhbnQ=,size_16,color_FFFFFF,t_70)

**哪如何去改变这个 value 值呢 ?**

##### 第三个知识点来了：**反射**

赶紧给面试官陪着笑脸说刚才激动了，代码我能不能再改改？

> 面试官：可以，代码本来就是一个不断优化的过程,你改吧！

然后又是一顿奋笔疾书，再次唰唰唰写了如下代码：

```java
public static void swap(Integer i, Integer j) throws NoSuchFieldException, IllegalAccessException {
    /*int temp = i;
        i = j;
        j = temp;*/
    Field value = Integer.class.getDeclaredField("value");
    int temp = i.intValue();
    value.set(i,j.intValue());
    value.set(j,temp);
}
```

这次长脑子呢，我又回过头检查了一遍代码，没办法，很蛋疼，这要是有电脑先跑一遍再说。

白纸只能靠你自己脑子想，脑子编译，脑子运行（当然运行不好可能就烧坏了）

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112458614.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1pCeWxhbnQ=,size_16,color_FFFFFF,t_70)

果然，查出问题来了（还好够机智）

前边不是说了这个 value 是私有属性么，既然是 private 的 ，final 的，在 Java 中是不允许的，再访问的时候会报

` java.lang.IllegalAccessException`异常，

在反射的时候还需要加`value.setAccessible(true)`，设置代码执行时绕过对私有属性的检查，哪么代码就变成了如下：

```java
public static void swap(Integer i, Integer j) throws NoSuchFieldException, IllegalAccessException {
    /*int temp = i;
        i = j;
        j = temp;*/
    Field value = Integer.class.getDeclaredField("value");
    value.setAccessible(true);
    int temp = i.intValue();
    value.set(i,j.intValue());
    value.set(j,temp);
}
```

另外多提几句：设置了 `setAccessible(true)`就能访问到私有属性是因为他的源码是这样的

```java
public void setAccessible(boolean flag) throws SecurityException {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
    setAccessible0(this, flag);
}
```

可以看到，他调用了 `setAccessible0()`这个方法，继续看下这个：

```java
private static void setAccessible0(AccessibleObject obj, boolean flag)
        throws SecurityException
    {
        if (obj instanceof Constructor && flag == true) {
            Constructor<?> c = (Constructor<?>)obj;
            if (c.getDeclaringClass() == Class.class) {
                throw new SecurityException("Cannot make a java.lang.Class" +
                                            " constructor accessible");
            }
        }
        obj.override = flag;
    }
```

这段代码我们需要关注有两点：

* 参数是 `boolean flag` 而这个 flag 实际的值恰好是我们设置进去的`setAccessible()`中的参数
* 这个参数真正的作用是把一个 `AccessibleObject` 对象的 `override`属性进行了赋值

哪么这个 `override`属性的作用又是什么呢？

我们一起来看下` value.set() `这个方法的源码

```java
@CallerSensitive
    public void set(Object obj, Object value)
        throws IllegalArgumentException, IllegalAccessException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        getFieldAccessor(obj).set(obj, value);
    }
```

看着这段代码是不瞬间就明白了，原来这个 `overried` 属性就好比一个开关，负责控制在 `set`值得时候是否需要检查访问权限（很多时候，一直说要阅读源码阅读源码，因为源码就好比火眼金睛，在源码面前，很多妖魔鬼怪都是无所遁形的）



看着这段代码乐开了花，心里想着这下应该总能交换了吧，我又非常自信的把代码递给了面试官

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112519317.gif)

> 面试官：为什么要这么改？为什么要使用反射？为什么要加这行 setAccessible(true) ？

哇 此时正和我意啊，写了这半天，就等你问我这几个点，于是我很利索的把上边描述的给面试官讲了一遍，他听完之后继续微微一笑，这个笑很迷，也很渗人。



难道这还不对？

他又开始发问：

> 面试官：这段代码还是会有问题，最终输出结果会是 a = 2, b = 2。可以提示你一下，你知道拆箱装箱吗？

呃，这还涉及到拆箱装箱了... ...

##### 第四个知识点：拆箱装箱

我们在上面的代码中 

```java
Integer a = 1, b = 2;
```

a 和 b 是 Integer 类型，但是 1 和 2 是 int 类型，为什么把 int 赋值给 Integer 不报错？

 因为 Java 中有自动装箱（如果感兴趣的话可以使用 javap 命令去查看一下这行代码执行的字节码）

```java
实际上 Integer a = 1 就相当于执行了 Integer a = Integer.valueOf(1);
```

哪么，`valueOf()`方法的实现又是什么样的呢？

```java
 public static Integer valueOf(int i) {
     if (i >= IntegerCache.low && i <= IntegerCache.high)
         return IntegerCache.cache[i + (-IntegerCache.low)];
     return new Integer(i);
 }
```

这个方法的代码说明，如果你的值是在某个范围之内，会从 `IntegerCache`这个缓存中获取值，而不是去 new 一个新的 `Integer`对象。继续研究 `IntegerCache`这个类

```java
static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // high value may be configured by property
            int h = 127;
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.max(i, 127);
                    // Maximum array size is Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;

            cache = new Integer[(high - low) + 1];
            int j = low;
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);

            // range [-128, 127] must be interned (JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }
```

根据以上代码可以得到：**在范围在 -128 - 127 之间的数字，会被直接初始化好之后直接加入得到缓存中，之后处于这个范围中的所有Integer 会直接从缓存获取值，这样提高了访问效率**



为了验证这一点，你可以直接试一试，写一段代码：

```java
Integer a = 100,b = 100;
System.out.println(a == b);
```

根据我们在 Java 领域的理解，对于引用类型，使用 == 比较的是他们在内存中的地址，哪么，对于Integer这个引用类型，直接使用 == 结果应该是` false`。

可是，你如果实际调试试一试的话，会发现这是 `true`, 是不是有点不可思议？

哪么为什么是` ture` ,就回到了上边说的缓存问题，因为 100 处于 -128-127 这份范围

如果你定义的变量是

```java 
Integer a = 200,b = 200;
System.out.println(a == b);
```

这个结果输出肯定是 `false`，因为根据前边`Integer。valueOf()`实现的源码可以得到：超过-128-127的值需要重新 `new Integer(i)`,但凡是 `new` 出来的，使用 `==` 比肯定是 `false`

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112542376.jpg)



继续深究下去你会发现面试官说的 a = 2, b = 2 是对的，具体原因是：

```java
public static void swap(Integer i, Integer j) throws NoSuchFieldException, IllegalAccessException {
        Field value = Integer.class.getDeclaredField("value");
        value.setAccessible(true);
        int temp = i.intValue();
    	// 此处 我们使用 j.intValue 返回结果是个 int 类型数据 
    	// 而 value.set()方法需要的是一个 Object 对象 此处就涉及到了装箱 
    	// 所以 i 值的实际变化过程为：i = Integer.valueOf(j.intValue()).intValue()
        value.set(i,j.intValue());
    	// 同理 j 值得实际变化过程为：j = Integer.valueOf(temp).intValue()
    	// 因为 valueOf() 要从缓存获取值 也就是此时需要根据 temp 的下标来获取值
    	// 可是在上一步中 i 的值已经被自动装箱之后变成了 2 
    	// 所以此处会把 j 的值设置成 2
        value.set(j,temp);
    }
```

**综上：我们就搞清楚了为什么面试官会说结果是 a = 2, b = 2 .**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112553631.jpg)

既然分析出了为什么会变成 a = 2 b = 2，哪就好办呢。

发现问题，解决问题，永远是程序员最优秀的品质，对了，还要脸皮厚（小声哔哔）

我又厚着脸把代码要过来了（面试官还是一如既往的微笑，一如既往很迷的笑）

##### 第五个知识点：如何避免拆箱和装箱操作

* 把 set 改为 setInt 避免装箱操作

  ```java
  public static void swap(Integer i, Integer j) throws NoSuchFieldException, IllegalAccessException {
          Field value = Integer.class.getDeclaredField("value");
          value.setAccessible(true);
          int temp = i.intValue();
          value.setInt(i,j.intValue());
          value.setInt(j,temp);
      }
  }
  ```

* 把 temp 重新创建一个对象进行赋值，这样就不会和 i 的值产生相互影响

  ```java
  public static void swap(Integer i, Integer j) throws NoSuchFieldException, IllegalAccessException {
          Field value = Integer.class.getDeclaredField("value");
          value.setAccessible(true);
          int temp = new Integer(i.intValue());
          value.setInt(i,j.intValue());
          value.setInt(j,temp);
      }
  ```



靠着脸厚，我第三次把代码交给了面试官，没办法，厚度不是你所能想象的... ...

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019022211260537.jpg)



这一次，他终于不再笑了，不再很迷的笑了

看来这场面试要迎来终结了 ... ...

> 面试官：嗯，你总算答对了，现在来总结一下这道题涉及到的知识点（这是要考察表达能力啊）

##### 总结：

* 值传递和引用传递
* Integer 实现缓存细节
* 使用反射修改私有属性的值
* 拆箱和装箱

有没有不总结不知道，一总结吓一跳的感觉，这么一道看似简单的题，竟然考察到了这么多东西

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190222112736939.jpg)

> 面试官：好了，技术问题我们今天就先面到这里，接下来能否说一说你有什么长处？
>
> 我：我是一个思想积极乐观向上的人。
>
> 面试官：能否举个例子。
>
> 我：什么时候开始上班？

>
>Github地址：https://github.com/Bylant/LeetCode
>个人网站地址：http://www.zhoudl.top
>CSDN地址：https://blog.csdn.net/ZBylant
>微信公众号 ![微信公众号](http://images.zhoudl.top/WeChat_public_number/business_card_07.png)