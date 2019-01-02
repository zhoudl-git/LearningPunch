> Multiversion concurrency control 多版本并发控制
>
> 并发访问（读或者写）数据库时，对正在事务内处理的数据做多版本的管理，用来避免由于写操作的堵塞，而引发读操作失败的并发问题。

### 引言

先看一个案例：

1.查看数据的事务隔离级别

对事务隔离级别不熟悉的同学可以参考文章 [【MySQL （三） | 五分钟搞清楚MySQL事务隔离级别】](https://mp.weixin.qq.com/s/WIqoR0-l7h9SObIzmGDatQ)

```sql
SELECT @@tx_isolation;
```

![查看数据库的事务隔离级别](http://pkon92vqd.bkt.clouddn.com/1-%E6%9F%A5%E7%9C%8B%E6%95%B0%E6%8D%AE%E5%BA%93%E4%BA%8B%E5%8A%A1%E9%9A%94%E7%A6%BB%E7%BA%A7%E5%88%AB.png)

可见 数据库隔离级别使用的是MySQL默认的RR级别。

**REPEATABLE READ** 意味着：

* 同一个事务中多次执行同一个select,读取到的数据没有发生改变；
* 此时：允许幻读，但不允许不可重复读和脏读，所以RR隔离级别要求解决不可重复读；

2.在不同会话中执行以下SQL

补充一下建表语句：

```sql
create table `test_zq` (
	`id` int (11),
	`test_id` int (11)
); 
insert into `test_zq` (`id`, `test_id`) values('1','18');
insert into `test_zq` (`id`, `test_id`) values('4','8');
insert into `test_zq` (`id`, `test_id`) values('7','4');
insert into `test_zq` (`id`, `test_id`) values('10','1234');
```

用户1：

```sql
begin;
-- 更新 id 为 1 的数据
UPDATE test_zq SET test_id = 20 WHERE id = 1;
```

用户2:

```sql
begin;
--查询 id 为 1 的数据
SELECT * FROM test_zq WHERE id = 1;
```

执行结果大致如下：

![执行结果](http://pkon92vqd.bkt.clouddn.com/2-MVCC%E6%A1%88%E4%BE%8B%E6%BC%94%E7%A4%BA1.png)

根据事务隔离级别来看，我们理论上对获得 X 锁（关于锁的概念可以参考 [【MySQL （四） | 五分钟搞清楚InnoDB锁机制】](http://mp.weixin.qq.com/s?__biz=MzI1Mzg4OTMxNQ==&mid=100000550&idx=1&sn=8a5cdff008fc1eed7b5c623c1bdf4ed1&chksm=69ccdd6a5ebb547c7b7baf6be78763fc5065e0a58de202f3e25d8d8ed56e6d1c1146332cfec1#rd)）的数据行是不能再被获取读锁而访问的，但是事实上我们依然访问到了这个数据！

**通过结果说明**：我们可以在一个事务未进行 commit/rollback操作之前，另一个事务仍然可以读取到数据库中的数据，只不过是读取到的是其他事务未改变之前的数据。此处是利用了MVCC多数据做了多版本处理，读取的数据来源于快照。

3.同理，在不同会话中执行以下SQL

用户1：

```sql
begin;
SELECT * FROM test_zq WHERE id = 1;
```

用户2：

```sql
begin;
update test_zq set test_id = 22 where id = 1;
```

执行完之后再回到用户1进行一次数据查询

```sql
SELECT * FROM test_zq WHERE id = 1;
```

执行结果：

![执行结果2](http://pkon92vqd.bkt.clouddn.com/3-MVCC%E6%A1%88%E4%BE%8B%E6%BC%94%E7%A4%BA2.png)

执行结果和上一步的执行结果一样，只不过区别在于2步骤中是先 update 后 select , 3 步骤是先 select 后 update.

虽然两者执行结果是一致的，但是我们要思考两个问题：

* 他们的底层实现是一样的吗？
* 他们的实现和MVCC有什么关系呢？

接下来我们便开始了解一下 MVCC 机制

### 什么是MVCC

> MVCC，Multi-Version Concurrency Control，多版本并发控制。MVCC 是一种并发控制的方法，一般在数据库管理系统中，实现对数据库的并发访问；在编程语言中实现事务内存。

如果有人从数据库中读数据的同时，有另外的人写入数据，有可能读数据的人会看到『半写』或者不一致的数据。有很多种方法来解决这个问题，叫做并发控制方法。最简单的方法，通过加锁，让所有的读者等待写者工作完成，但是这样效率会很差。MVCC 使用了一种不同的手段，每个连接到数据库的读者，**在某个瞬间看到的是数据库的一个快照**，写者写操作造成的变化在写操作完成之前（或者数据库事务提交之前）对于其他的读者来说是不可见的。

当一个 MVCC 数据库需要更一个一条数据记录的时候，它不会直接用新数据覆盖旧数据，而是将旧数据标记为过时（obsolete）并在别处增加新版本的数据。这样就会有存储多个版本的数据，但是只有一个是最新的。这种方式允许读者读取在他读之前已经存在的数据，即使这些在读的过程中半路被别人修改、删除了，也对先前正在读的用户没有影响。**这种多版本的方式避免了填充删除操作在内存和磁盘存储结构造成的空洞的开销，但是需要系统周期性整理（sweep through）以真实删除老的、过时的数据。**对于面向文档的数据库（Document-oriented database，也即半结构化数据库）来说，这种方式允许系统将整个文档写到磁盘的一块连续区域上，当需要更新的时候，直接重写一个版本，而不是对文档的某些比特位、分片切除，或者维护一个链式的、非连续的数据库结构。

MVCC 提供了时点（point in time）一致性视图。MVCC 并发控制下的读事务一般使用**时间戳或者事务 ID**去标记当前读的数据库的状态（版本），读取这个版本的数据。读、写事务相互隔离，不需要加锁。**读写并存的时候，写操作会根据目前数据库的状态，创建一个新版本，并发的读则依旧访问旧版本的数据。**



一句话总结就是：

> MVCC(`Multiversion concurrency control`) 就是 同一份数据临时保留多版本的一种方式，进而实现并发控制

哪么此处需要注意的点就是：

* 在读写并发的过程中如何实现多版本？
* 在读写并发之后，如何实现旧版本的删除（毕竟很多时候只需要一份最新版的数据就够了）？

下面介绍一下MySQL中对于 MVCC 的逻辑实现

### MVCC逻辑流程-插入

在MySQL中建表时，每个表都会有三列隐藏记录，其中和MVCC有关系的有两列

* 数据行的版本号 （DB_TRX_ID）
* 删除版本号 (DB_ROLL_PT)

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|      |         |           |            |

在插入数据的时候，假设系统的全局事务ID从1开始，以下SQL语句执行分析参考注释信息：

```sql
begin;-- 获取到全局事务ID
insert into `test_zq` (`id`, `test_id`) values('5','68');
insert into `test_zq` (`id`, `test_id`) values('6','78');
commit;-- 提交事务
```

当执行完以上SQL语句之后，表格中的内容会变成：

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|  5   |   68    |     1     |    NULL    |
|  6   |   78    |     1     |    NULL    |

可以看到，插入的过程中会把全局事务ID记录到列 DB_TRX_ID 中去

### MVCC逻辑流程-删除

对上述表格做删除逻辑，执行以下SQL语句（假设获取到的事务逻辑ID为 3）

```sql
begin；--获得全局事务ID = 3
delete test_zq where id = 6;
commit;
```

执行完上述SQL之后数据并没有被真正删除，而是对删除版本号做改变，如下所示：

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|  5   |   68    |     1     |    NULL    |
|  6   |   78    |     1     |     3      |

### MVCC逻辑流程-修改

修改逻辑和删除逻辑有点相似，修改数据的时候 会先复制一条当前记录行数据，同事标记这条数据的数据行版本号为当前是事务版本号，最后把原来的数据行的删除版本号标记为当前是事务。

执行以下SQL语句：

```sql
begin;-- 获取全局系统事务ID 假设为 10
update test_zq set test_id = 22 where id = 5;
commit;
```

执行后表格实际数据应该是：

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|  5   |   68    |     1     |     10     |
|  6   |   78    |     1     |     3      |
|  5   |   22    |    10     |    NULL    |

### MVCC逻辑流程-查询

此时，数据查询规则如下：

- 查找**数据行版本号早于当前事务版本号**的数据行记录

  也就是说，数据行的版本号要小于或等于当前是事务的系统版本号，这样也就确保了读取到的数据是当前事务开始前已经存在的数据，或者是自身事务改变过的数据

- 查找**删除版本号**要么为NULL，要么**大于当前事务版本号**的记录

  这样确保查询出来的数据行记录在事务开启之前没有被删除

根据上述规则，我们继续以上张表格为例，对此做查询操作

```sql
begin;-- 假设拿到的系统事务ID为 12
select * from test_zq;
commit;
```

执行结果应该是：

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|  6   |   22    |    10     |    NULL    |

### MySQL 中 MVCC 版本控制案例

回到文章刚开始的哪个例子，我们使用 MVCC 机制分析一遍

为了方便描述，对SQL语句做如下标记：

```sql
begin;--假设当前获取到的事务 ID 为 2				 ----1
select * from test_zq;						    ----2
commit;

begin;--假设当前获取到的事务 ID 为 3				 ----3
UPDATE test_zq SET test_id = 20 WHERE id = 1;	----4
commit;
```

对表中数据做初始化：

```sql
begin;
insert into `test_zq` (`id`, `test_id`) values('1','18');
insert into `test_zq` (`id`, `test_id`) values('4','8');
commit;
```



表中的原始数据为：

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|  1   |   18    |     1     |    NULL    |
|  4   |    8    |     1     |    NULL    |

#### 案例1

**执行顺序为** `1 2 3 4 2`

`1 2` 步骤执行结果为：

|  id  | test_id |
| :--: | :-----: |
|  1   |   18    |
|  4   |    8    |

`3 4` 步骤执行结果为：

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|  1   |   18    |     1     |     3      |
|  4   |    8    |     1     |    NULL    |
|  1   |   20    |     3     |    NULL    |

`2 `执行后的结果为：

|  id  | test_id |
| :--: | :-----: |
|  1   |   18    |
|  4   |    8    |

上述结果符合预期，接下来看案例2

#### 案例2

**执行顺序为**` 3 4 1 2`

`3 4` 步骤执行后结果为：

|  id  | test_id | DB_TRX_ID | DB_ROLL_PT |
| :--: | :-----: | :-------: | :--------: |
|  1   |   18    |     1     |     3      |
|  4   |    8    |     1     |    NULL    |
|  1   |   20    |     3     |    NULL    |

`1 2` 步骤执行后结果为：

假设此时的事务ID为 `txid = 4`

则查询结果是 ：

|  id  | test_id |
| :--: | :-----: |
|  1   |   20    |
|  4   |    8    |

显然，结果应该是不对的，但是我们在文章开头也是按照这样的顺序执行的，但是MySQL的返回结果没有任何问题，可是这里根据MVCC机制来分析却出现了这样的状况，所以问题出在哪里？

我们大概可以猜测到：

> 此处问题不是出在 MVCC 机制，MySQL解决不可重复读和脏读并不是单纯利用 MVCC 机制来实现的。

限于篇幅，这个问题留到下一篇文章，下一篇将会讨论 Undo Log 和 Redo Log等。