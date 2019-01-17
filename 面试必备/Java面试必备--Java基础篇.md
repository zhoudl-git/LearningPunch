## 第一章 Java 基础知识

###  HashMap和Hashtable的区别

1. 两者最主要的区别在于 **Hashtable是线程安全，而HashMap则非线程安全**

Hashtable 的实现方法里面都添加了 synchronized 关键字来确保线程同步，因此相对而言 HashMap 性能会高一些，平时使用时若无特殊需求建议使用 HashMap，在多线程环境下若使用 HashMap 需要使用`Collections.synchronizedMap()`方法来获取一个线程安全的集合（`Collections.synchronizedMap()`实现原理是 Collections 定义了一个 SynchronizedMap 的内部类，这个类实现了Map接口，在调用方法时使用synchronized 来保证线程同步,当然了实际上操作的还是我们传入的 HashMap 实例，简单的说Collections.synchronizedMap() 方法在操作HashMap时自动添加了 synchronized 来实现线程同步，类似Collections.synchronizedXX方法也是此原理）
HashMap可以使用null作为key，而Hashtable则不允许null作为key
虽说HashMap支持null值作为key，不过建议还是尽量避免这样使用，因为一旦不小心使用了，若因此引发一些问题，排查起来很是费事

2. HashMap 以 null 作为key时，总是存储在 table 数组的第一个节点上

* HashMap 是对 Map接口的实现，HashTable 实现了Map接口和Dictionary抽象类
* HashMap 的初始容量为16，Hashtable初始容量为11，两者的填充因子默认都是0.75
  HashMap 扩容时是当前容量翻倍即:`capacity*2，Hashtable扩容时是容量翻倍+1即:capacity*2+1`
* 两者计算 hash 的方法不同
  Hashtable 计算 hash 是直接使用 key 的 hashcode 值对数组的长度直接进行取模
  `int hash = key.hashCode(); int index = (hash & 0x7FFFFFFF) % tab.length`;
* HashMap计算hash对key的hashcode值进行了二次hash，以获得更好的散列值，然后对数组长度取模，JDK8将key的hashcode高16位异或下来，然后对数组长度取模
```java
staticint hash(int h) {
// This function ensures that hashCodes that differ only by 
// constant multiples at each bit position have a bounded 
// number of collisions (approximately 8 at default load factor).
    h ^= (h >>> 20) ^ (h >>> 12); 
    return h ^ (h >>> 7) ^ (h >>> 4); 
} 
staticint indexFor(int h, int length) { 
    return h & (length-1); 
}
```
3. HashMap和Hashtable的底层实现都是数组+链表结构实现，添加、删除、获取元素时都是先计算hash，根据hash和table.length计算index也就是table数组的下标，然后进行相应操作Hashtable,HashMap,ConcurrentHashMap,HashSet底层实现原理与线程安全问题

   * Hashtable是线程安全的，它的方法是同步了的，可以直接用在多线程环境中
   *  HashMap则不是线程安全的。在多线程环境中，需要手动实现同步机制
   * Hashtable中采用的锁机制是一次锁住整个hash表，从而同一时刻只能由一个线程对其进行操作；
   * 在迭代方面，ConcurrentHashMap当iterator被创建后集合再发生改变就不再是抛出ConcurrentModificationException，取而代之的是在改变时new新的数据从而不影响原有的数据。iterator完成后再将头指针替换为新的数据。这样iterator线程可以使用原来老的数据。而写线程也可以并发的完成改变 

   > ConcurrentHashMap使用锁分段技术，一次锁住一个桶。ConcurrentHashMap是由Segment数组结构和HashEntry数组结构组成。Segment是一种可重入锁ReentrantLock，在ConcurrentHashMap里扮演锁的角色，HashEntry则用于存储键值对数据。一个ConcurrentHashMap里包含一个Segment数组，Segment的结构和HashMap类似，是一种数组和链表结构， 一个Segment里包含一个HashEntry数组，每个HashEntry是一个链表结构的元素， 每个Segment守护者一个HashEntry数组里的元素,当对HashEntry数组的数据进行修改时，必须首先获得它对应的Segment锁。ConcurrentHashMap默认将hash表分为16个桶，诸如get,put,remove等常用操作只锁当前需要用到的桶。原来只能一个线程进入，现在却能同时有16个写线程执行，并发性能的提升是显而易见的(Spring容器Bean的初始化：为BeanFactory新建XmlBeanDefinitionReader，加载并从Root开始解析xml配置文件，并遍历各级的节点。根据类型的不同的处理，最后都会集中在处理bean类型的节点。将bean节点映射成BeanDefinitionHolder，并在BeanFactory中注册，key为bean的name，value为BeanDefinition对象。由BeanFactory的ConcurrentHashMap类型的成员变量持有) 

   * HashSet不是key value结构，仅仅是存储不重复的元素，相当于简化版的HashMap，只是包含HashMap中的key而已.HashSet内部就是使用HashMap实现，只不过HashSet里面的HashMap所有的value都是同一个Object而已，因此HashSet也是非线程安全的，至于HashSet和Hashtable的区别，HashSet就是个简化的HashMap的

### 高性能场景下，Map家族的优化使用建议

考虑加载因子地设定初始大小
减小加载因子
String类型的key，不能用==判断或者可能有哈希冲突时，尽量减少长度
使用定制版的EnumMap
使用IntObjectHashMap

### Java集合类：list、set、map、queue实现类

List系（ArrayList, LinkedList, Vector, Stack等）；
Map系（HashMap,LinkedHashMap,TreeMap, WeakHashMap, EnumMap等）；
Set系（HashSet, LinkedHashSet, TreeSet）; queue系（ArrayBlockingQueue, ConcurrentLinkedQueue, DelayQueue, LinkedBlockingDeque, LinkedBlockingQueue, LinkedTransferQueue, PriorityBlockingQueue, PriorityQueue, SynchronousQueue）；
工具类（Collections,Arrays）

 **Java容器类类库的用途是"保存对象"，并将其划分为两个不同的概念：**

* Collection 一组"对立"的元素，通常这些元素都服从某种规则

  - List必须保持元素特定的顺序

  - Set不能有重复元素

  - Queue保持一个队列(FIFO先进先出)的顺序

* Map 一组成对的"键值对"对象



Collection和Map的区别在于容器中每个位置保存的元素个数:
1. Collection 每个位置只能保存一个元素(对象) 
2. Map保存的是"键值对"，就像一个小型数据库。我们可以通过"键"找到该键对应的"值"

#### Java集合类架构层次关系

![attachments-2018-07-zqFfnPBb5b44c9aa2df1b.jpg](https://gper.gupaoedu.com/server-img/attachments/2018/07/zqFfnPBb5b44c9aa2df1b.jpg)

#### 集合类的编程应用场景

1.  HashSet的性能总是比TreeSet好(特别是最常用的添加、查询元素等操作)，因为TreeSet需要额外的红黑树算法来维护集合元素的次序。只有当需要一个保持排序的Set时，才应该使用TreeSet，否则都应该使用HashSet 

2. 对于普通的插入、删除操作，LinkedHashSet比HashSet要略慢一点，这是由维护链表所带来的开销造成的。不过，因为有了链表的存在，遍历LinkedHashSet会更快 

3. EnumSet是所有Set实现类中性能最好的，但它只能保存同一个枚举类的枚举值作为集合元素 

4. `HashSet、TreeSet、EnumSet`都是"线程不安全"的，通常可以通过Collections工具类的`synchronizedSortedSet `方法来"包装"该Set集合 

   `SortedSet s = Collections.synchronizedSortedSet(new TreeSet(...));` 

   * java提供的List就是一个"线性表接口"，ArrayList(基于数组的线性表)、LinkedList(基于链的线性表)是线性表的两种典型实现
   * Queue代表了队列，Deque代表了双端队列(既可以作为队列使用、也可以作为栈使用)
   * 因为数组以一块连续内存来保存所有的数组元素，所以数组在随机访问时性能最好。所以的内部以数组作为底层实现的集合在随机访问时性能最好
   * 内部以链表作为底层实现的集合在执行插入、删除操作时有很好的性能 
   * 进行迭代操作时，以链表作为底层实现的集合比以数组作为底层实现的集合性能好

#### 描述一下ArrayList和LinkedList各自实现和区别

由于ArrayList是基于数组实现的，而数组是一块连续的内存空间，如果在数组的任意位置插入元素，必然导致在该位置后的所有元素需要重新排列，因此，其效率相对会比较低。

1. ArrayList是实现了基于动态数组的数据结构，LinkedList基于链表的数据结构

2. 对于随机访问get和set，ArrayList优于LinkedList，因为LinkedList要移动指针

3. 对于新增和删除操作add和remove，LinkedList比较占优势，因为ArrayList要移动数据

4. 对ArrayList和LinkedList而言，在列表末尾增加一个元素所花的开销都是固定的。对ArrayList而言，主要是在内部数组中增加一项，指向所添加的元素，偶尔可能会导致对数组重新进行分配；而对LinkedList而言，这个开销是统一的，分配一个内部Entry对象

5. 在ArrayList的中间插入或删除一个元素意味着这个列表中剩余的元素都会被移动；而在LinkedList的中间插入或删除一个元素的开销是固定的

6. LinkedList不支持高效的随机元素访问

7. ArrayList的空间浪费主要体现在在list列表的结尾预留一定的容量空间，而LinkedList的空间花费则体现在它的每一个元素都需要消耗相当的空间

> 可以这样说：当操作是在一列数据的后面添加数据而不是在前面或中间,并且需要随机地访问其中的元素时,使用ArrayList会提供比较好的性能；当你的操作是在一列数据的前面或中间添加或删除数据,并且按照顺序访问其中的元素时,就应该使用LinkedList了

#### Java中的队列都有哪些，有什么区别

1. BlockingQueue有四个具体的实现类，根据不同需求，选择不同的实现类：

2. ArrayBlockingQueue：基于数组结构的有界阻塞队列，规定大小的BlockingQueue，其构造函数必须带一个int参数来指明其大小。 其所含的对象是以FIFO（先入先出）顺序排序的

3. LinkedBlockingQueue：基于链表结构的阻塞队列，大小不定的BlockingQueue，若其构造函数带一个规定大小的参数，生成的BlockingQueue有大小限制，若不带大小参数，所生成的BlockingQueue的大小由Integer.MAX_VALUE来决定。其所含的对象是以FIFO顺序排序的，吞吐量通常要高于ArrayBlockingQueue

4. PriorityBlockingQueue：具有优先级的无限阻塞队列，类似于LinkedBlockingQueue,但其所含对象的排序不是FIFO，而是依据对象的自然排序顺序或者是构造函数所带的Comparator决定的顺序

5. SynchronousQueue：不存储元素的阻塞队列，对其的操作必须是放和取交替完成的，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQueue

6. LinkedBlockingQueue和ArrayBlockingQueue比较起来，它们背后所用的数据结构不一样，导致LinkedBlockingQueue的数据吞吐量要大于ArrayBlockingQueue，但在线程数量很大时其性能的可预见性低于ArrayBlockingQueue

### 反射中，Class.forName和classloader的区别

Class的装载分了三个阶段，loading，linking和initializing
Class.forName(className)调用Class.forName(className, true, this.getClass().getClassLoader())。第二个参数，是指Class被loading后是不是必须被初始化
ClassLoader.loadClass(className)调用ClassLoader.loadClass(name, false)，第二个参数指出Class是否被link
LoadClass()方法加载类及初始化过程： 
类加载(loadclass())(加载)——>>newInstance()(链接+初始化) 
newInstance(): 
(开始连接)静态代码块——>>普通变量分配准备(a=0;b=0;c=null)——>>(开始初始化)普通变量赋值(a=1;b=2;c="haha")——>>构造方法——>>初始化成功
Class.forName(Stirng className)一个参数方法加载类及初始化过程： 
类加载(Class.forName())(加载)——>>静态代码块——>>newInstance()(链接+初始化) 
newInstance():
(开始连接)普通变量分配准备(a=0;b=0;c=null)——>>(开始初始化)普通变量赋值(a=1;b=2;c="haha")——>>构造方法——>>初始化成功
区别:Class.forName(className)装载的class已经被初始化，而ClassLoader.loadClass(className)装载的class还没有被link。 ClassLoader 加载类时不会进行类的初始化， Class.forName()会强制初始化该类。
一般情况下，这两个方法效果一样，都能装载Class。但如果程序依赖于Class是否被初始化，就必须用Class.forName(name)

### String、Stringbuilder、Stringbuffer区别

|                      | String               | StringBuffer   | StringBuilder  |
| -------------------- | -------------------- | -------------- | -------------- |
| Storage Area存储区   | Constant String Pool | Heap           | Heap           |
| Modifiable是否可修改 | No (immutable)       | Yes( mutable ) | Yes( mutable ) |
| Thread Safe线程安全  | Yes                  | Yes            | No             |
| Performance性能      | Fast                 | Very slow      | Fast           |

1. 在执行速度方面的比较：StringBuilder > StringBuffer

2. StringBuffer与StringBuilder，他们是字符串变量，是可改变的对象，每当我们用它们对字符串做操作时，实际上是在一个对象上操作的，不像String一样创建一些对象进行操作，所以速度就快了

3. StringBuilder：线程非安全的
     StringBuffer：线程安全的
     当我们在字符串缓冲去被多个线程使用是，JVM不能保证 StringBuilder 的操作是安全的，虽然他的速度最快，但是可以保证 StringBuffer 是可以正确操作的。当然大多数情况下就是我们是在单线程下进行的操作，所以大多数情况下是建议用 StringBuilder 而不用 StringBuffer 的，就是速度的原因。
     对于三者使用的总结：
     * 如果要操作少量的数据用 : String
     * 单线程操作字符串缓冲区下操作大量数据 : StringBuilder
     * 多线程操作字符串缓冲区下操作大量数据 : StringBuffer

### String字符串变量的连接动作

在编译阶段会被转化成 StringBuilder 的 append 操作，变量最终指向 Java 堆上新建的 String 对象

1. String中使用 + 字符串连接符进行字符串连接时，连接操作最开始时如果都是字符串常量，编译后将尽可能多的直接将字符串常量连接起来，形成新的字符串常量参与后续连接

     2. 接下来的字符串连接是从左向右依次进行，对于不同的字符串，首先以最左边的字符串为参数创建StringBuilder对象，然后依次对右边进行append操作，最后将StringBuilder对象通过toString()方法转换成String对象（注意：中间的多个字符串常量不会自动拼接）

​     也就是说：

​     `String c = "xx" + "yy " + a + "zz" + "mm" + b;`

​     实质上的实现过程是： 

```java
String c = new StringBuilder("xxyy").append(a).append("zz").append("mm").append(b).toString();
```

​     由此得出结论：

> 当使用+进行多个字符串连接时，实际上是产生了一个StringBuilder对象和一个String对象。

 JDK1.7与之前版本的Java String源码相比，String类减少了int offset 和 int count的定义。

这样变化的结果主要体现在：

    * 避免之前版本的String对象subString时可能引起的内存泄露问题；
    * 新版本的subString时间复杂度将有O(1)变为O(n);

### Java 四种引用类型

#### StrongReference

​        java默认的引用类型,如果不特意使用java.lang.ref下的类,那么程序中的所有引用都是强引用。有强引用存在的对象永远都不会被gc收集,所以在内存不够用时,JVM宁愿抛出OutOfMemoryError这样的错误，也不愿意将强引用对象进行回收

####  SoftReference

​        软引用不会保证对象一定不会被回收，只能最大可能保证。如果内存有剩余，那么软引用对象不会被回收，如果内存不足，那么gc会回收软引用对象。所以这种特性可以用来实现缓存技术。软引用要用java.lang.ref.SoftReference来实现。

####  WeakReference

​        除了通过java.lang.ref.WeakReference来使用弱引用,WeakHashMap同样也利用了弱引用。弱引用一定会被gc回收,不管内存是否不足。

#### PhantomReference

​        幽灵引用，也叫虚引用。java.lang.ref.PhantomReference类中只有一个方法get(),而且几乎没有实现,只是返回null。而且这个类只有一个构造器(软引用和弱引用均有两个构造器)：

```java
public PhantomReference(T referent, ReferenceQueue<? super T> q) {  
	super(referent, q);  
}  
```

​    也就是说,幽灵引用只能与ReferenceQueue一起使用。如果一个对象仅有幽灵引用,那么它就像没有任何引用一样,在任何时候都可能被gc回收。幽灵引用主要用来跟踪对象被垃圾回收的活动。 

#### ReferenceQueue

​    如果一个对象只有软引用、弱引用或者幽灵引用,gc在回收对象时,JVM会自动将其引用放入一个ReferenceQueue中。WeakHashMap就是利用了ReferenceQueue来实现清除没有强引用Entry的
利用软引用和弱引用解决OOM问题：用一个HashMap来保存图片的路径和相应图片对象关联的软引用之间的映射关系，在内存不足时，JVM会自动回收这些缓存图片对象所占用的空间，从而有效地避免了OOM的问题.
通过软引用实现Java对象的高速缓存:比如我们创建了一Person的类，如果每次需要查询一个人的信息,哪怕是几秒中之前刚刚查询过的，都要重新构建一个实例，这将引起大量Person对象的消耗,并且由于这些对象的生命周期相对较短,会引起多次GC影响性能。此时,通过软引用和 HashMap 的结合可以构建高速缓存,提供性能.

### 抽象类和接口的区别

含有abstract修饰符的class即为抽象类，abstract类不能创建实例对象，含有abstract的方法的类必须定义为abstract class，abstract class 里的方法不必是抽象的，抽象来中定义抽象方法必须放在具体子类中实现，所以，不能有抽象的构造方法或抽象的静态方法，如果子类没有实现抽象父类中的所有方法，那么，子类也必须定义为**抽象类**
**接口（interface）**可以说成是抽象类的特例。接口中的所有方法都必须是抽象的，接口中的方法定义默认为public abstract 。接口中的变量是全局常量，即public static final修饰的。

* 抽象类里可以有构造方法，而接口内不能有构造方法
* 抽象类中可以有普通成员变量，而接口中不能有普通成员变量
* 抽象类中可以包含非抽象的普通方法，而接口中所有的方法必须是抽象的，不能有非抽象的普通方法
* 抽象类中的抽象方法的访问类型可以是public，protected和默认类型，但接口中的抽象方法只能是public类型的，并且默认即为public abstract类型。
* 抽象类中可以包含静态方法，接口内不能包含静态方法。
* 抽象类和接口中都可以包含静态成员变量，抽象类中的静态成员变量的访问类型可以任意，但接口中定义的变量只能是public static类型，并且默认为public static类型。
* 一个类可以实现多个接口，但只能继承一个抽象类
* 接口更多的是在系统框架设计方法发挥作用，主要定义模块之间的通信，而抽象类在代码实现方面发挥作用，可以实现代码的重用

### java的基础类型和字节大小

byte 1个字节 short 2个字节 char 2个字节 int 4个字节
long 8个字节 float 4个字节 double 8个字节
Integer的HashCode就是自己，Long要把高32位异或下来变成int， String则是循环累计结果＊31＋下一个字符，不过因为String是不可变对象，所以生成完一次就会自己cache起来

### final关键字的好处

final关键字提高了性能。JVM和Java应用都会缓存final变量。
final变量可以安全的在多线程环境下进行共享，而不需要额外的同步开销。
使用final关键字，JVM会对方法、变量及类进行优化。

### Java创建（实例化）对象的五种方式

* 用new语句创建对象，这是最常见的创建对象的方法。
* 通过工厂方法返回对象，如：String str = String.valueOf(23); 
* 运用反射手段,调用java.lang.Class或者java.lang.reflect.Constructor类的newInstance()实例方法。如：Object obj = Class.forName("java.lang.Object").newInstance(); 
* 调用对象的clone()方法。
* 通过I/O流（包括反序列化），如运用反序列化手段，调用java.io.ObjectInputStream对象的 readObject()方法。 

```
               类内部     package内       子类         其他 
public    		允许         允许         允许          允许 
protected       允许         允许         允许          不允许 
default         允许         允许         不允许     	 不允许 
private         允许         不允许       不允许     	 不允许
```
1. 对象默认都是从Eden区分配，但是遇到大对象会直接在Old区分配，此时不会进行YGC

2. 这个大对象是指：大于PretenureSizeThreshold或者大于Eden

3. 但是如果遇到待分配对象不是大对象，Eden区剩余空间不足，此时就会发生YGC

4. PretenureSizeThreshold值只是判断条件之一还有其他条件，判断条件的顺序不重要，不会影响最终的YGC的触发

5. 注意young GC中有部分存活对象会晋升到old gen，所以young GC后old gen的占用量通常会有所升高