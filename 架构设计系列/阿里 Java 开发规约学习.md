### 命名规范

1. 【强制】POJO类中布尔类型的变量，都不要加is，否则部分框架解析会引起序列化错误。 反例：定义为基本数据类型Boolean isDeleted；的属性，它的方法也是isDeleted()，RPC 框架在反向解析的时候，“以为”对应的属性名称是deleted，导致属性获取不到，进而抛出异常。

2. 【强制】包名统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词。包名统一使用单数形式，但是类名如果有复数含义，类名可以使用复数形式。 正例： 应用工具类包名为com.alibaba.open.util、类名为MessageUtils（此规则参考spring的框架结构）

### OOP 规约

1. 【强制】序列化类新增属性时，请不要修改serialVersionUID字段，避免反序列失败；如果完全不兼容升级，避免反序列化混乱，那么请修改serialVersionUID值。 说明：注意serialVersionUID不一致会抛出序列化运行时异常。
2. 【推荐】慎用Object的clone方法来拷贝对象。 说明：对象的clone方法默认是浅拷贝，若想实现深拷贝需要重写clone方法实现属性对象的拷贝。

### 集合处理

1. 【强制】使用集合转数组的方法，必须使用集合的toArray(T[] array)，传入的是类型完全一样的数组，大小就是list.size()。
   说明：使用toArray带参方法，入参分配的数组空间不够大时，toArray方法内部将重新分配内存空间，并返回新数组地址；如果数组元素大于实际所需，下标为[ list.size() ]的数组元素将被置为null，其它数组元素保持原值，因此最好将方法入参数组大小定义与集合元素个数一致。 正例：

   ```java
   List<String> list = new ArrayList<String>(2);
   list.add("guan");
   list.add("bao");
   String[] array = new String[list.size()];
   array = list.toArray(array);
   ```

   反例：直接使用toArray无参方法存在问题，此方法返回值只能是Object[]类，若强转其它类型数组将出现ClassCastException错误。

2. 【强制】使用工具类Arrays.asList()把数组转换成集合时，不能使用其修改集合相关的方法，它的add/remove/clear方法会抛出UnsupportedOperationException异常。 说明：asList的返回对象是一个Arrays内部类，并没有实现集合的修改方法。Arrays.asList体现的是适配器模式，只是转换接口，后台的数据仍是数组。 String[] str = new String[] { "you", "wu" }; List list = Arrays.asList(str); 第一种情况：list.add("yangguanbao"); 运行时异常。 第二种情况：str[0] = "gujin"; 那么list.get(0)也会随之修改。

### 并发处理

1. 【强制】线程资源必须通过线程池提供，不允许在应用中自行显式创建线程。 说明：使用线程池的好处是减少在创建和销毁线程上所花的时间以及系统资源的开销，解决资源不足的问题。如果不使用线程池，有可能造成系统创建大量同类线程而导致消耗完内存或者“过度切换”的问题。

2. 【强制】线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式，这样
   的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。
   说明： Executors 返回的线程池对象 的弊端 如下
   * FixedThreadPool 和 SingleThread Pool
     允许的请求队列长度为 Integer.MAX_VALUE，可 能会堆积大量的请求，从而导致 OOM。
   * CachedThreadPool 和 ScheduledThreadPool
     允许的创建线程数量为 Integer.MAX_VALUE 可能会创建大量的线程，从而导致 OOM。

3. 【强制】SimpleDateFormat 是线程不安全的类，一般不要定义为static变量，如果定义为static，必须加锁，或者使用DateUtils工具类。 正例：注意线程安全，使用DateUtils。亦推荐如下处理：

   ```java
   private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
       @Override
       protected DateFormat initialValue() {
       	return new SimpleDateFormat("yyyy-MM-dd");
       }
   };
   ```

   说明：如果是JDK8的应用，可以使用Instant代替Date，LocalDateTime代替Calendar，DateTimeFormatter代替SimpleDateFormat，官方给出的解释：simple beautiful strong immutable thread-safe。

### 注释规范

1. 【推荐】与其“半吊子”英文来注释，不如用中文注释把问题说清楚。专有名词与关键字保持英文原文即可。 反例：“TCP连接超时”解释成“传输控制协议连接超时”，理解反而费脑筋。

### 其他

1. 【强制】注意 Math.random() 这个方法返回是double类型，注意取值的范围 0≤x<1（能够取到零值，注意除零异常），如果想获取整数类型的随机数，不要将x放大10的若干倍然后取整，直接使用Random对象的nextInt或者nextLong方法。
2. 【推荐】任何数据结构的构造或初始化，都应指定大小，避免数据结构无限增长吃光内存。

### 异常规范

1. 【强制】异常不要用来做流程控制，条件控制，因为异常的处理效率比条件分支低。

2. 【强制】有try块放到了事务代码中，catch异常后，如果需要回滚事务，一定要注意手动回滚事务。

3. 【推荐】方法的返回值可以为null，不强制返回空集合，或者空对象等，必须添加注释充分说明什么情况下会返回null值。调用方需要进行null判断防止NPE问题。 说明：本手册明确防止NPE是调用者的责任。即使被调用方法返回空集合或者空对象，对调用者来说，也并非高枕无忧，必须考虑到远程调用失败、序列化失败、运行时异常等场景返回null的情况。

4. 【推荐】防止NPE，是程序员的基本修养，注意NPE产生的场景： 
   * 返回类型为基本数据类型， return 包装数据类型的对象时，自动拆箱有可能产生 NPE 。
   * 数据库的查询结果可能为null。
   * 集合里的元素即使isNotEmpty，取出的数据元素也可能为null。 
   *  远程调用返回对象时，一律要求进行空指针判断，防止NPE。 
   *  对于Session中获取的数据，建议NPE检查，避免空指针。 
   *  级联调用obj.getA().getB().getC()；一连串调用，易产生NPE。
   
   > 正例：使用JDK8的Optional类来防止NPE问题。
   >
   > 反例：`public int f() { return Integer对象}`， 如果为null，自动解箱抛NPE。

5. 【参考】在代码中使用“抛异常”还是“返回错误码”，对于公司外的http/api开放接口必须使用“错误码”；而应用内部推荐异常抛出；跨应用间RPC调用优先考虑使用Result方式，封装isSuccess()方法、“错误码”、“错误简短信息”。 说明：关于RPC方法返回方式使用Result方式的理由： 1）使用抛异常返回方式，调用方如果没有捕获到就会产生运行时错误。 2）如果不加栈信息，只是new自定义异常，加入自己的理解的error message，对于调用端解决问题的帮助不会太多。如果加了栈信息，在频繁调用出错的情况下，数据序列化和传输的性能损耗也是问题。

### 日志规约

1. 【强制】对trace/debug/info级别的日志输出，必须使用条件输出形式或者使用占位符的方式。 说明：logger.debug("Processing trade with id: " + id + " and symbol: " + symbol); 如果日志级别是warn，上述日志不会打印，但是会执行字符串拼接操作，如果symbol是对象，会执行toString()方法，浪费了系统资源，执行了上述操作，最终日志却没有打印。 正例：（条件）

   ```java
   if (logger.isDebugEnabled()) {
   	logger.debug("Processing trade with id: " + id + " and symbol: " + symbol);
   }
   ```

   正例：（占位符）

   ```java
   logger.debug("Processing trade with id: {} and symbol : {} ", id, symbol);
   ```

2. 【强制】异常信息应该包括两类信息：案发现场信息和异常堆栈信息。如果不处理，那么通过关键字throws往上抛出。 正例：logger.error(各类参数或者对象toString + "_" + e.getMessage(), e);

3. 【推荐】谨慎地记录日志。生产环境禁止输出debug日志；有选择地输出info日志；如果使用warn来记录刚上线时的业务行为信息，一定要注意日志输出量的问题，避免把服务器磁盘撑爆，并记得及时删除这些观察日志。 说明：大量地输出无效日志，不利于系统性能提升，也不利于快速定位错误点。记录日志时请思考：这些日志真的有人看吗？看到这条日志你能做什么？能不能给问题排查带来好处？

### MySQL 规约

1. 【强制】主键索引名为pk_字段名；唯一索引名为uk_字段名；普通索引名则为idx_字段名。 说明：pk_ 即primary key；uk_ 即 unique key；idx_ 即index的简称。
2. 【强制】小数类型为decimal，禁止使用float和double。 说明：float和double在存储的时候，存在精度损失的问题，很可能在值的比较时，得到不正确的结果。如果存储的数据范围超过decimal的范围，建议将数据拆成整数和小数分开存储。
3. 【强制】表必备三字段：id, gmt_create, gmt_modified。 说明： 其中 id必为主键，类型为 unsigned bigint、单表时自增、步长为 1 。 gmt_create, gmt_modified的类型均为 date_time类型，前者现在时表示主动创建，后者过去分词表示被
   动更新。
4. 【强制】数据订正时，删除和修改记录时，要先select，避免出现误删除，确认无误才能执行更新语句。

#### ORM 映射

1. 【强制】POJO类的布尔属性不能加is，而数据库字段必须加is_，要求在resultMap中进行字段与属性之间的映射。