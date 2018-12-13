### 事务

#### 什么是事务？

数据库操作的最小工作单元，事务是一组不可再分割的操作集合（工作逻辑单元）

`begin/start transaction `开启事务

`commit/rollback` 提交事务

`set session autocommit = on/off`



#### 事务并发带来了哪些问题？

* 脏读

* 幻读

* 不可重复读

#### 事务四种隔离级别

Read uncommitter(未提交读)

Read Committer(提交读) ：解决了脏读

Repeatable Read(可重复读)： 解决了不可重复读和脏读

Serializable(串行化) ：全部解决了

##### innodb的支持程度

##### 隔离级别如何实现的？

锁、MVCC

### 锁

> 锁是用于管理不同事务对共享资源的并发访问

表锁和行锁的区别：

##### 共享锁 Shared Locks  S

##### 排他锁 Exclusive Locks X

##### 行锁

行锁锁的是索引上的索引项

只有通过索引条件进行数据检索，Innodb才使用行级锁。否则，将使用表锁（锁住索引的所有记录）

###### 行锁的算法

* 临键锁 Next-Key : 左开右闭

  防止幻读

* 间隙锁 Gap : 当记录不存在时，临键锁退化成Gap

  Gap只在RR事务隔离中存在

* 记录锁 Record Lock :唯一性索引 条件为精准匹配，退化成Record锁



##### 意向共享锁 IS

##### 意向排他锁 IX

##### 自增锁 AUTO-INC Locks



