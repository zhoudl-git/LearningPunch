### 相关链接

https://blog.csdn.net/u013252072/article/details/52912385

https://blog.csdn.net/kaka1121/article/details/53395587

https://blog.csdn.net/derrantcm/article/details/51534411



### 索引相关

##### 为什么选用B + Tree

B+树是B-树的变种（PLUS版）多路绝对平衡查找树，他拥有B-树的优势
B+树扫库、表能力更强
B+树的磁盘读写能力更强
B+树的排序能力更强
B+树的查询效率更加稳定（仁者见仁、智者见智）



### 存储引擎相关

##### MySQL中myisam与innodb的区别

* InnoDB支持事务，而MyISAM不支持事务

* InnoDB支持行级锁，而MyISAM支持表级锁

* InnoDB支持MVCC, 而MyISAM不支持

* InnoDB支持外键，而MyISAM不支持

* InnoDB不支持全文索引，而MyISAM支持。

##### innodb引擎的4大特性

* 插入缓冲（insert buffer)

* 二次写(double write)

* 自适应哈希索引(ahi)

* 预读(read ahead)

##### 2者selectcount(*)哪个更快，为什么

myisam更快，因为myisam内部维护了一个计数器，可以直接调取。

##### innodb的事务与日志的实现方式

###### 有多少种日志？

* 错误日志：记录出错信息，也记录一些警告信息或者正确的信息。
* 查询日志：记录所有对数据库请求的信息，不论这些请求是否得到了正确的执行。
* 慢查询日志：设置一个阈值，将运行时间超过该值的所有SQL语句都记录到慢查询的日志文件中。
* 二进制日志：记录对数据库执行更改的所有操作。
* 中继日志：
* 事务日志：

###### 事务的4种隔离级别

* 读未提交(RU)
* 读已提交(RC)
* 可重复读(RR)
* 串行

###### 事务是如何通过日志来实现的，说得越深入越好

事务日志是通过redo和innodb的存储引擎日志缓冲（Innodb log buffer）来实现的，当开始一个事务的时候，会记录该事务的lsn(log sequence number)号; 当事务执行时，会往InnoDB存储引擎的日志
的日志缓存里面插入事务日志；当事务提交时，必须将存储引擎的日志缓冲写入磁盘（通过innodb_flush_log_at_trx_commit来控制），也就是写数据前，需要先写日志。这种方式称为“预写日志方式”

##### innodb的读写参数优化

###### 读取参数
`global buffer pool以及 local buffer；`

###### 写入参数；
`innodb_flush_log_at_trx_commit`
`innodb_buffer_pool_size`

###### 与IO相关的参数；
`innodb_write_io_threads = 8`
`innodb_read_io_threads = 8`
`innodb_thread_concurrency = 0`

###### 缓存参数以及缓存的适用场景。
`query cache/query_cache_type`
并不是所有表都适合使用query cache。造成query cache失效的原因主要是相应的table发生了变更

- 第一个：读操作多的话看看比例，简单来说，如果是用户清单表，或者说是数据比例比较固定，比如说商品列表，是可以打开的，前提是这些库比较集中，数据库中的实务比较小。
- 第二个：我们“行骗”的时候，比如说我们竞标的时候压测，把query cache打开，还是能收到qps激增的效果，当然前提示前端的连接池什么的都配置一样。大部分情况下如果写入的居多，访问量并不多，那么就不要打开，例如社交网站的，10%的人产生内容，其余的90%都在消费，打开还是效果很好的，但是你如果是qq消息，或者聊天，那就很要命。
- 第三个：小网站或者没有高并发的无所谓，高并发下，会看到 很多 qcache 锁 等待，所以一般高并发下，不建议打开query cache

##### MySQL中InnoDB引擎的行锁是通过加在什么上完成(或称实现)的？为什么是这样子的？

InnoDB是基于索引来完成行锁
例: select * from tab_with_index where id = 1 for update;
for update 可以根据条件来完成行锁锁定,并且 id 是有索引键的列,
如果 id 不是索引键那么InnoDB将完成表锁,,并发将无从谈起

