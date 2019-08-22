在数据库设计的时候，我们经常会需要设计时间字段，在 MYSQL 中，时间字段可以使用 int、timestamp、datetime 三种类型来存储，那么这三种类型哪一种用来存储时间性能比较高，效率好呢 ？

就这个问题，来一个实践出真知吧。

#### 一、准备工作

###### 1.1 建表

```sql
CREATE TABLE IF NOT EXISTS `datetime_test` (
  `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=1,
  `d_int` int(11) NOT NULL DEFAULT '0',
  `d_timestamp` timestamp NULL DEFAULT NULL,
  `d_datetime` datetime DEFAULT NULL
) ENGINE=MyISAM AUTO_INCREMENT=1000001 DEFAULT CHARSET=utf8;
```

###### 1.2 插入100万条测试数据 

```sql
//插入d_intvalue=1到100万之间的数据
insert into datetime_test(d_int,d_timestamp,d_datetime) 
values(d_intvalue,FROM_UNIXTIME(d_intvalue),FROM_UNIXTIME(d_intvalue));
```

取中间的 20 万条做查询测试：

```sql
SELECT FROM_UNIXTIME(400000), FROM_UNIXTIME(600000)
1970-01-05 23:06:40, 1970-01-08 06:40:00
```

#### 二、MyISAM引擎 

###### 2.1 MyISAM 引擎无索引下的d_int/d_timestamp/d_datetime 

###### 2.1.1 int 类型是否调用UNIX_TIMESTAMP优化对比 

```sql
//SQL_NO_CACHE意思是说查询时不适用缓存
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_int >400000 AND d_int<600000

查询花费 0.0780 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_int>UNIX_TIMESTAMP('1970-01-05 23:06:40') 
AND d_int<UNIX_TIMESTAMP('1970-01-08 06:40:00')

查询花费 0.0780 秒
```

效率不错

###### 2.1.2 timestamp 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_timestamp>'1970-01-05 23:06:40' 
AND d_timestamp<'1970-01-08 06:40:00'

查询花费 0.4368 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE UNIX_TIMESTAMP(d_timestamp)>400000 
AND UNIX_TIMESTAMP(d_timestamp)<600000

查询花费 0.0780 秒
```

对于 timestamp 类型，使用UNIX_TIMESTAMP内置函数查询效率很高，几乎和int相当；直接和日期比较效率低。

###### 2.1.3 datetime类型是否调用UNIX_TIMESTAMP优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_datetime>'1970-01-05 23:06:40' 
AND d_datetime<'1970-01-08 06:40:00'
查询花费 0.1370 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE UNIX_TIMESTAMP(d_datetime)>400000 
AND UNIX_TIMESTAMP(d_datetime)<600000
查询花费 0.7498 秒
```

对于 datetime 类型，使用 UNIX_TIMESTAMP 内置函数查询效率很低，不建议；直接和日期比较，效率还行。

###### 2.2 MyISAM引擎有索引下的 d_int/d_timestamp/d_datetime 

###### 2.2.1 int 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_int >400000
AND d_int<600000
查询花费 0.3900 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_int>UNIX_TIMESTAMP('1970-01-05 23:06:40') 
AND d_int<UNIX_TIMESTAMP('1970-01-08 06:40:00')
查询花费 0.3824 秒
```

对于 int 类型，有索引的效率反而低了，笔者估计是由于设计的表结构问题，多了索引，反倒多了一个索引查找。

###### 2.2.2 timestamp 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_timestamp>'1970-01-05 23:06:40' 
AND d_timestamp<'1970-01-08 06:40:00'
查询花费 0.5696 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE UNIX_TIMESTAMP(d_timestamp)>400000 
AND UNIX_TIMESTAMP(d_timestamp)<600000
查询花费 0.0780 秒
```

对于 timestamp 类型，有没有索引貌似区别不大。

###### 2.2.3 datetime 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE d_datetime>'1970-01-05 23:06:40' 
AND d_datetime<'1970-01-08 06:40:00'
查询花费 0.4508 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test` 
WHERE UNIX_TIMESTAMP(d_datetime)>400000 
AND UNIX_TIMESTAMP(d_datetime)<600000
查询花费 0.7614 秒
```

对于 datetime 类型，有索引反而效率低了。

#### 三、InnoDB引擎

###### 3.1 InnoDB引擎无索引下的 d_int/d_timestamp/d_datetime 

###### 3.1.1 int 类型是否调用 UNIX_TIMESTAMP优化对比 

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE d_int >400000 
AND d_int<600000
查询花费 0.3198 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` WHERE d_int>UNIX_TIMESTAMP('1970-01-05 23:06:40') 
AND d_int<UNIX_TIMESTAMP('1970-01-08 06:40:00')
查询花费 0.3092 秒
```

InnoDB 引擎的查询效率明细比 MyISAM 引擎的低，低 3 倍+。

###### 3.1.2 timestamp 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE d_timestamp>'1970-01-05 23:06:40' 
AND d_timestamp<'1970-01-08 06:40:00'
查询花费 0.7092 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE UNIX_TIMESTAMP(d_timestamp)>400000 
AND UNIX_TIMESTAMP(d_timestamp)<600000
查询花费 0.3160 秒
```

对于 timestamp 类型，使用 UNIX_TIMESTAMP 内置函数查询效率同样高出直接和日期比较。

###### 3.1.3 datetime 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE d_datetime>'1970-01-05 23:06:40' 
AND d_datetime<'1970-01-08 06:40:00'
查询花费 0.3834 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE UNIX_TIMESTAMP(d_datetime)>400000 
AND UNIX_TIMESTAMP(d_datetime)<600000
查询花费 0.9794 秒
```

对于 datetime 类型，直接和日期比较，效率高于 UNIX_TIMESTAMP 内置函数查询。

###### 3.2 InnoDB 引擎无索引下的d_int/d_timestamp/d_datetime

###### 3.2.1 int 类型是否调用 UNIX_TIMESTAMP 优化对比 

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE d_int >400000 
AND d_int<600000
查询花费 0.0522 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE d_int>UNIX_TIMESTAMP('1970-01-05 23:06:40') 
AND d_int<UNIX_TIMESTAMP('1970-01-08 06:40:00')
查询花费 0.0624 秒
```

InnoDB引 擎有了索引之后，性能较 MyISAM 有大幅提高。

###### 3.2.2 timestamp 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE d_timestamp>'1970-01-05 23:06:40' 
AND d_timestamp<'1970-01-08 06:40:00'
查询花费 0.1776 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE UNIX_TIMESTAMP(d_timestamp)>400000 
AND UNIX_TIMESTAMP(d_timestamp)<600000
查询花费 0.2944 秒
```

对于 timestamp 类型，有了索引，反倒不建议使用 MYSQL 内置函数UNIX_TIMESTAMP 查询了。

###### 3.2.3 datetime 类型是否调用 UNIX_TIMESTAMP 优化对比

```sql
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE d_datetime>'1970-01-05 23:06:40' 
AND d_datetime<'1970-01-08 06:40:00'
查询花费 0.0820 秒
SELECT SQL_NO_CACHE count(id) FROM `datetime_test2` 
WHERE UNIX_TIMESTAMP(d_datetime)>400000 
AND UNIX_TIMESTAMP(d_datetime)<600000
查询花费 0.9994 秒
```

对于 datetime 类型，同样有了索引，反倒不建议使用 MYSQL 内置函数UNIX_TIMESTAMP 查询了。

#### 四、总结

- 对于 MyISAM 引擎，不建立索引的情况下（推荐），效率从高到低：int > UNIX_TIMESTAMP(timestamp) > datetime（直接和时间比较）> timestamp（直接和时间比较）> UNIX_TIMESTAMP(datetime) 。
- 对于 MyISAM 引擎，建立索引的情况下，效率从高到低： UNIX_TIMESTAMP(timestamp) > int > datetime（直接和时间比较）>timestamp（直接和时间比较）>UNIX_TIMESTAMP(datetime) 。
- 对于 InnoDB 引擎，没有索引的情况下(不建议)，效率从高到低：int > UNIX_TIMESTAMP(timestamp) > datetime（直接和时间比较） > timestamp（直接和时间比较）> UNIX_TIMESTAMP(datetime)。
- 对于 InnoDB 引擎，建立索引的情况下，效率从高到低：int > datetime（直接和时间比较） > timestamp（直接和时间比较）> UNIX_TIMESTAMP(timestamp) > UNIX_TIMESTAMP(datetime)。
- 一句话，对于 MyISAM 引擎，采用 UNIX_TIMESTAMP(timestamp) 比较；对于InnoDB 引擎，建立索引，采用 int 或 datetime直接时间比较。



作者：爱情小傻蛋

链接：https://www.jianshu.com/p/27c807ed6974