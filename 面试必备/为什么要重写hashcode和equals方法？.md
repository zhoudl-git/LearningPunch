### 引言

以前面试的时候被面试官问到过这样一个问题：

> 你有没有重写过 hashCode 方法？

心里想着我没事重写哪玩意干啥，能不写就不写。嘴上当然没敢这么说，只能略表遗憾的说抱歉，我没写过。

撇了面试官一眼，明显看到他对这个回答不满意，但是这已经触及到我的知识盲点了，我也很惭愧，可是确实没有重写过,咱也不能胡扯不是。

然后他又问到另外一个问题：

> 你在用 HashMap 的时候，键（Key）部分，有没有放过自定义对象？

我说我放过，很自信的说我放过（其实我忘了我有没有放过），但是不能怂啊，第一个都不会了，第二个再说不会哪不是直接拜拜要走人了吗？

面试官狡猾的笑了，说是你既然没有重写过 hashCode 方法，你怎么把自定义对象放进去的？

我勒个去，原来你在这等着我呢，没想到这还是个连环炮，惹不起惹不起，认怂三连

![](http://images.zhoudl.top/0315/09777303.jpg)

不过不会就学，不懂就问，这一直都是咱程序猿优秀的素养，今天就干脆从 Hash 表学起，讲述 HashMap 的存取数据规则，由此来搞定上述问题的答案。

###  通过 Hash 算法来了解 HashMap 对象的高效性

我们先复习数据结构里的一个知识点：

> 在一个长度为 n（假设是100）的线性表（假设是 ArrayList）里，存放着无序的数字；如果我们要找一个指定的数字，就不得不通过从头到尾依次遍历来查找，这样的平均查找次数是 n / 2（这里是50）。

我们再来观察 Hash 表（这里所说的 Hash 表纯粹是数据结构上的概念，和  Java 无关）。

> 哈希表就是一种以 键-值(key-indexed) 存储数据的结构，我们只要输入待查找的值即 key，即可查找到其对应的值。

它的平均查找次数接近于 1，代价相当小。

使用哈希查找有两个步骤:

1.  使用哈希函数将被查找的键转换为数组的索引：在理想的情况下，不同的键会被转换为不同的索引值，但是在有些情况下我们需要处理多个键被哈希到同一个索引值的情况。所以哈希查找的第二个步骤就是处理冲突
2.  处理哈希碰撞冲突：有很多处理哈希碰撞冲突的方法，本文后面会介绍拉链法和线性探测法。

既然哈希查找第一步就是使用哈希函数将键映射成索引，那我们就先假设一个 Hash 函数是` x * x % 5`，（当然实际编程中不可能用这么简单的 Hash 函数，一般选择的哈希函数都是要易于计算并且能够均匀分布所有键的，这里纯粹为了说明方便），然后假设 Hash 表是一个长度是 11 的线性表。

接下来如果我们如果要把 6 放入其中，那么我们首先会对 6 用 Hash 函数计算一下，结果是 1，所以我们就把 6 放入到索引号是 1 这个位置。同样如果我们要放数字 7，经过 Hash 函数计算，7 的结果是 4，那么它将被放入索引是 4 的这个位置。

如下如所示：

![哈希表](http://images.zhoudl.top/0315/1552616238675.png)

这样做的好处非常明显：比如我们要从中找 6 这个元素，我们可以先通过 Hash 函数计算 6 的索引位置，然后直接从 1 号索引里找到它了。不过我们有可能会遇到**Hash值冲突**这个问题，比如经过 Hash 函数计算后，7 和 8 会有相同的 Hash 值，此时我们就需要了解一下解决哈希碰撞的几种常见方式：

#### 开放地址法

使用某种探查(亦称探测)技术在散列表中形成一个探查序列。沿此序列逐个单元地查找，直到找到给定的关键字，或者碰到一个开放的地址(即该地址单元为空)为止（若要插入，在探查到开放的地址，则可将待插入的新结点存入该地址单元）。

按照形成探查序列的方法不同，可将开放定址法区分为线性探查法、线性补偿探测法以及随机探测等。限于篇幅，我们此处只讨论线性探查法。

##### 线性探查法

该方法基本思想是：

将散列表 T[0..m-1] 看成是一个循环向量，若初始探查的地址为d(即h(key)=d)，则最长的探查序列为：

```
d，d+l，d+2，…，m-1，0，1，…，d-1
```

即 : 探查时从地址 d 开始，首先探查 T[d]，然后依次探查 T[d+1]，…，直到 T[m-1]，此后又循环到 T[0]，T[1],…，直到探查到 T[d-1] 为止。
探查过程终止于三种情况：

* 若当前探查的单元为空，则表示查找失败（若是插入则将 key 写入其中）；

* 若当前探查的单元中含有 key，则查找成功，但对于插入意味着失败；

* 若探查到 T[d-1] 时仍未发现空单元也未找到 key，则无论是查找还是插入均意味着失败(此时表满)。

  

利用开放地址法的一般形式，线性探查法的探查序列为：

```
 hi = (h(key)+i)％m 0≤i≤m-1 // 即di=i
```

用线性探测法处理冲突，思路清晰，算法简单，但存在下列缺点：

1. 处理溢出需另编程序。一般可另外设立一个溢出表，专门用来存放上述哈希表中放不下的记录。此溢出表最简单的结构是顺序表，查找方法可用顺序查找。
2. 按上述算法建立起来的哈希表，删除工作非常困难。假如要从哈希表 HT 中删除一个记录，按理应将这个记录所在位置置为空，但我们不能这样做，而只能标上已被删除的标记，否则，将会影响以后的查找。
3. 
   线性探测法很容易产生堆聚现象。所谓堆聚现象，就是存入哈希表的记录在表中连成一片。按照线性探测法处理冲突，如果生成哈希地址的连续序列愈长 ( 即不同关键字值的哈希地址相邻在一起愈长 ) ，则当新的记录加入该表时，与这个序列发生冲突的可能性愈大。因此，哈希地址的较长连续序列比较短连续序列生长得快，这就意味着，一旦出现堆聚 ( 伴随着冲突 ) ，就将引起进一步的堆聚。

在使用了上述线性探查法的情况下，则 7 和 8 在存储的时候，因为两者哈希后得到的索引一致，并且 7 已经存到了哈希表中，哪么 8 在找到索引 4 的时候会发现已经有值了，则它继续开始往后查找，此时找到索引为 5 的位置发现为空，它就会把 8 放到索引为 5 的位置上，如下：

![使用线性探查法解决哈希冲突](http://images.zhoudl.top/0315/1552617550188.png)

#### 链地址法

拉链法解决冲突的做法是：将所有关键字为同义词的结点链接在同一个单链表中。若选定的散列表长度为 m，则可将散列表定义为一个由 m 个头指针组成的指针数 组 T[0..m-1]。凡是散列地址为 i 的结点，均插入到以 T[i] 为头指针的单链表中。T 中各分量的初值均应为空指针。在拉链法中，装填因子 α 可以大于 1，但一般均取 α≤1。

与开放定址法相比，拉链法有如下几个优点：

1. 拉链法处理冲突简单，且无堆积现象，即非同义词决不会发生冲突，因此平均查找长度较短；
2. 由于拉链法中各链表上的结点空间是动态申请的，故它更适合于造表前无法确定表长的情况；
3. 开放定址法为减少冲突，要求装填因子 α 较小，故当结点规模较大时会浪费很多空间。而拉链法中可取 α≥1，且结点较大时，拉链法中增加的指针域可忽略不计，因此节省空间；
4. 在用拉链法构造的散列表中，删除结点的操作易于实现。只要简单地删去链表上相应的结点即可。而对开放地址法构造的散列表，删除结点不能简单地将被删结 点的空间置为空，否则将截断在它之后填人散列表的同义词结点的查找路径。这是因为各种开放地址法中，空地址单元(即开放地址)都是查找失败的条件。因此在 用开放地址法处理冲突的散列表上执行删除操作，只能在被删结点上做删除标记，而不能真正删除结点。

使用拉链法的时候 7 和 8 的时候具体的做法是：为所有 Hash 值是 i 的对象建立一个同义词链表。假设我们在放入 8 的时候，发现 4 号位置已经被占，那么就会新建一个链表结点放入 8。同样，如果我们要找 8，那么发现 4 号索引里不是 8，那会沿着链表依次查找。

存储位置如下：

![使用拉链法解决哈希冲突](http://images.zhoudl.top/0315/1552618159233.png)



 Java 中的 HashMap 对象采用的是**链地址法**的解决方案。

虽然我们还是无法彻底避免 Hash 值冲突的问题，但是 Hash 函数设计合理，仍能保证同义词链表的长度被控制在一个合理的范围里。这里讲的理论知识并非无的放矢，大家能在后文里清晰地了解到重写 hashCode 方法的重要性。

### 2 为什么要重写 equals 和 hashCode 方法

当我们用 HashMap 存入自定义的类时，如果不重写这个自定义类的 equals 和 hashCode 方法，得到的结果会和我们预期的不一样。

我们来看一个例子,定义一个 HashMapKey.java 的类，这个类只有一个属性 id ：

```java
public class HashMapKey {

    private Integer id;

    public HashMapKey(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
```

测试类如下：

```java
public class TestHashMap {

    public static void main(String[] args) {
        HashMapKey k1 = new HashMapKey(1);
        HashMapKey k2 = new HashMapKey(1);
        HashMap<HashMapKey, String> map = new HashMap<>();
        map.put(k1, "程序猿杂货铺");
        System.out.println("map.get(k2) : " + map.get(k2));
    }

}
```

在 main 函数里，我们定义了两个 HashMapKey 对象，它们的 id 都是 1，然后创建了一个 HashMap 对象，紧接着我们通过 put 方法把 k1 和一串字符放入到 map里，最后用 k2 去从 HashMap 里得到值，因为 k1 和 k2 值是一样的，理论上我们是可以用这个键获取到对应的值的，看似符合逻辑，实则不然，它的执行结果是：

```
map.get(k2) : null
```

![](http://images.zhoudl.top/0315/09D6AC84.jpg)

其实出现这个情况的原因有两个：

* 没有重写 hashCode 方法
* 没有重写 equals 方法。

当我们往 HashMap 里放 k1 时，首先会调用 HashMapKey 这个类的 hashCode 方法计算它的 hash 值，随后把 k1 放入 hash 值所指引的内存位置。

但是我们没有在 HashMapKey 里重写 hashCode 方法，所以这里调用的是 Object 类的 hashCode 方法，而 Object 类的 hashCode 方法返回的 hash 值其实是 k1 对象的内存地址（假设是 0x100）。

  ![k1 和 k2 在 map 中存储的概念模型](http://images.zhoudl.top/0315/1552619614241.png)

如果我们随后是调用 map.get(k1)，那么我们会再次调用 hashCode 方法（还是返回 k1 的地址 0x100），随后根据得到的 hash 值，能很快地找到 k1。

但我们这里的代码是 map.get(k2)，当我们调用Object类的 hashCode方法（因为 HashMapKey 里没定义）计算 k2 的 hash值时，其实得到的是 k2 的内存地址（假设是 0x200）。由于 k1 和 k2 是两个不同的对象，所以它们的内存地址一定不会相同，也就是说它们的 hash 值一定不同，这就是我们无法用 k2 的 hash 值去拿 k1 的原因。

接下来我们在类 HashMapKey 中重写 hashCode 方法

```java
@Override
public int hashCode() {
   return id.hashCode();
}
```

此时因为 hashCode 方法返回的是 id 的 hash值，所以此处 k1 和 k2 这两个对象的 hash 值就变得相等了。

但是问题还没有结束，我们再来更正一下存 k1 和 取 k2 的动作。存 k1 时，是根据它 id 的 hash 值，假设这里是 103，把 k1 对象放入到对应的位置。而取 k2 时，是先计算它的 hash 值（由于 k2 的 id 也是 1，这个值也是 103），随后到这个位置去找。但运行结果还是会出乎我们意料：

```
map.get(k2) : null
```

明明 103号位置已经有 k1，但打印输出结果依然是 null。

![](http://images.zhoudl.top/0315/09E512FA.gif)

其实原因就是没有重写 HashMapKey 对象的 equals 方法。

HashMap 是用**链地址法**来处理冲突，也就是说，在 103号位置上，有可能存在着多个用链表形式存储的对象。它们通过 hashCode 方法返回的 hash 值都是 103。

![k1 和 k2 存储在哈希表中存储的概念模型](http://images.zhoudl.top/0315/1552620245018.png)

当我们通过 k2 的 hashCode 到 103号位置查找时，确实会得到 k1。但 k1 有可能仅仅是和 k2 具有相同的 hash值，但未必和 k2 相等，这个时候，就需要调用 HashMapKey 对象的 equals 方法来判断两者是否相等了。

由于我们在 HashMapKey 对象里没有定义 equals 方法，系统就不得不调用 Object 类的 equals 方法，同理由于 Object 的固有方法是根据两个对象的内存地址来判断，所以 k1 和 k2 一定不会相等，这就是为什么通过 map.get(k2) 依然得到 null 的原因。

为了解决这个问题，我们继续重写 equals 方法，在这个方法里，只要两个对象都是 Key 类型，而且它们的 id 相等，它们就相等。

```java
@Override
public boolean equals(Object o) {
     if (o == null || !(o instanceof HashMapKey)) {
         return false;
     } else {
         return this.getId().equals(((HashMapKey) o).getId());
     }
}
```

至此，问题已经解决。

![](http://images.zhoudl.top/0315/09ED7FD7.jpg)

### 总结

我们平时在项目开发中经常会用到 HashMap，虽然很多时候我们都会尽可能避免去在键值存放自定义对象，但是正因为如此，一旦碰到需要存放自定义对象了就容易出问题，重申一遍：如果你需要要在 HashMap 的“键”部分存放自定义的对象，一定要重写 equals 和 hashCode 方法。

其实 这个问题本身不难，只要我们平时稍微注意以下就可以避免，本文也是大概总结了以下，避免大家以后碰到了踩坑，希望对你有所帮助，保不齐下次面试也有人问你同样的问题。

