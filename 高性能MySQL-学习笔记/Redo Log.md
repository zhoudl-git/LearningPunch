> 原文地址：https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html

### 引言

为了最大程度避免数据写入时 IO 瓶颈带来的性能问题，MySQL 采用了这样一种缓存机制：

当修改数据库内数据时，InnoDB 先将该数据从磁盘读物到内存中，修改内存中的数据拷贝，并将该修改行为持久化到磁盘上的事务日志（先写 redo log buffer,在定期批量写入），而不是每次都直接将修改过的数据记录到磁盘内，等事务日志持久化完成之后，内存中的脏数据可以慢慢刷会磁盘，称之为 Write-Ahead Logging(预写式日志)。

事务日志采用的是追加写入，顺序 IO 会带来更好的性能优势，请参考文章 ... ...



为了避免脏数据刷回到磁盘过程中，掉电或系统故障带来的数据丢失问题，InnoDB 采用事务日志来解决这些问题。

innodb 事务日志包括 redo log 和 undo log。redo log是重做日志，提供前滚操作，undo log 是回滚日志，提供回滚操作。

undo Log 不是 redo log的逆向过程，其实它们都算是用来恢复的日志：

* redo log 通常是物理日志，记录的是数据页的物理修改，而不是某一行或某几行修改成怎样怎样，它用来恢复提交后的物理数据页(恢复数据页，且只能恢复到最后一次提交的位置)
* undo 用来回滚行记录到某个版本。undo log 一般是逻辑日志，根据每行记录进行记录

### redo log

#### 基本概念

redo log包括两部分：一是内存中的日志缓冲(redo log buffer)，该部分日志是易失性的；二是磁盘上的重做日志文件(redo log file)，该部分日志是持久的。

在概念上，innodb 通过 **force log at commit** 机制实现事务的持久性，即在事务提交的时候，必须先将该事务的所有事务日志写入到磁盘上的 redo log file 和 undo log file 中进行持久化。

为了确保每次日志都能写入到事务日志文件中，在每次将 log buffer 中的日志写入日志文件的过程中都会调用一次操作系统的fsync操作(即fsync()系统调用)。因为 MariaDB/MySQL 是工作在用户空间的，MariaDB/MySQL 的 log buffer 处于用户空间的内存中。要写入到磁盘上的 log file 中(redo:ib_logfileN文件,undo:share tablespace或.ibd文件)，中间还要经过操作系统内核空间的 OS buffer，调用fsync()的作用就是将 OS buffer 中的日志刷到磁盘上的 log file 中。

也就是说，从redo log buffer写日志到磁盘的redo log file中，过程如下： 

![图片来源网络 侵权联系删除](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508101949424-938931340.png)



在此处需要注意一点，一般所说的 log file 并不是磁盘上的物理日志文件，而是操作系统缓存中的log file，官方手册上的意思也是如此(例如：With a value of 2, the contents of the **InnoDB log buffer are written to the log file** after each transaction commit and **the log file is flushed to disk approximately once per second**)。但说实话，这不太好理解，既然都称为 file 了，应该已经属于物理文件了。所以在本文后续内容中都以 os buffer 或者 file system buffer 来表示官方手册中所说的 Log file，然后 log file 则表示磁盘上的物理日志文件，即 log file on disk。

另外，之所以要经过一层os buffer，是因为 open 日志文件的时候， open 没有使用 O_DIRECT 标志位，该标志位意味着绕过操作系统层的 os buffer，IO直写到底层存储设备。不使用该标志位意味着将日志进行缓冲，缓冲到了一定容量，或者显式` fsync() `才会将缓冲中的刷到存储设备。使用该标志位意味着每次都要发起系统调用。比如写 abcde，不使用 o_direct 将只发起 1 次系统调用，使用 o_object 将发起 5 次系统调用。

MySQL 支持用户自定义在 commit 时如何将 log buffer 中的日志刷 log file 中。这种控制通过变量 `innodb_flush_log_at_trx_commit` 的值来决定。该变量有3种值：0、1、2，默认为 1。但注意，这个变量只是控制 commit 动作是否刷新 log buffer 到磁盘。



* 当设置为 1 的时候，事务每次提交都会将 log buffer 中的日志写入 os buffer 并调用 `fsync() `刷到 log file on disk中。这种方式即使系统崩溃也不会丢失任何数据，但是因为每次提交都写入磁盘，IO 的性能较差。

* 当设置为 0 的时候，事务提交时不会将 log buffer 中日志写入到 os buffer，而是每秒写入 os buffer 并调用`fsync()`写入到 log file on disk 中。也就是说设置为 0 时是(大约)每秒刷新写入到磁盘中的，当系统崩溃，会丢失 1 秒钟的数据。

* 当设置为 2 的时候，每次提交都仅写入到 os buffer，然后是每秒调用 fsync() 将 os buffer 中的日志写入到 log file on disk。

![图片来源网络 侵权联系删除](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508104623183-690986409.png)

注意，有一个变量` innodb_flush_log_at_timeout `的值为1秒，该变量表示的是刷日志的频率，很多人误以为是控制` innodb_flush_log_at_trx_commit `值为 0 和 2 时的 1 秒频率，实际上并非如此。测试时将频率设置为 5 和设置为 1，当 `innodb_flush_log_at_trx_commit `设置为 0 和 2 的时候性能基本都是不变的。关于这个频率是控制什么的，在后面的"[刷日志到磁盘的规则](file:///E:/onedrive/%E6%88%91%E7%9A%84%E5%AD%A6%E4%B9%A0/MySQL/MySQL%E7%AE%A1%E7%90%86%E7%AF%87/MySQL%E7%AE%A1%E7%90%86.docx#_%E5%88%B7%E6%97%A5%E5%BF%97%E5%88%B0%E7%A3%81%E7%9B%98%E7%9A%84%E8%A7%84%E5%88%99)"中会说。

在主从复制结构中，要保证事务的持久性和一致性，需要对日志相关变量设置为如下：

- **如果启用了二进制日志，则设置sync_binlog=1，即每提交一次事务同步写到磁盘中。**
- **总是设置innodb_flush_log_at_trx_commit=1，即每提交一次事务都写到磁盘中。**

上述两项变量的设置保证了：每次提交事务都写入二进制日志和事务日志，并在提交时将它们刷新到磁盘中。

选择刷日志的时间会严重影响数据修改时的性能，特别是刷到磁盘的过程。下例就测试了 `innodb_flush_log_at_trx_commit `分别为 0、1、2 时的差距。

```sql
--创建测试表
drop table if exists test_flush_log;
create table test_flush_log(id int,name char(50))engine=innodb;

--创建插入指定行数的记录到测试表中的存储过程
drop procedure if exists proc_batch_insert;
delimiter $$
create procedure proc_batch_insert(i int)
begin
    declare s int default 1;
    declare c char(50) default repeat('a',50);
    while s <= i do
        start transaction;
        insert into test_flush_log values(NULL,c);
        commit;
        set s = s + 1;
    end while;
end$$
delimiter ;

```

当前环境下，

```sql
-- 调用存储过程 100000 表示生成 100w 条记录
mysql> call proc_batch_insert(100000);
Query OK, 0 rows affected (15.48 sec)
```

结果是 15.48 秒。

再测试值为 2 的时候，即每次提交都刷新到 os buffer，但每秒才刷入磁盘中。

```sql
mysql> set @@global.innodb_flush_log_at_trx_commit = 2;    
mysql> truncate test_flush_log;

mysql> call proc(100000);
Query OK, 0 rows affected (3.41 sec)
```

结果插入时间大减，只需 3.41 秒。

最后测试值为0的时候，即每秒才刷到os buffer和磁盘。

```sql
mysql> set @@global.innodb_flush_log_at_trx_commit=0;
mysql> truncate test_flush_log;

mysql> call proc(100000);
Query OK, 0 rows affected (2.10 sec)
```

结果只有 2.10 秒。

最后可以发现，其实值为 2 和 0 的时候，它们的差距并不太大，但 2 却比 0 要安全的多。它们都是每秒从 os buffer 刷到磁盘，它们之间的时间差体现在 log buffer 刷到 os buffer 上。因为将 log buffer 中的日志刷新到 os buffer 只是内存数据的转移，并没有太大的开销，所以每次提交和每秒刷入差距并不大。可以测试插入更多的数据来比较，以下是插入 100W 行数据的情况。从结果可见，值为 2 和 0 的时候差距并不大，但值为 1 的性能却差太多。

![img](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508105836098-1767966445.png)

尽管设置为 0 和 2 可以大幅度提升插入性能，但是在故障的时候可能会丢失1秒钟数据，这1秒钟很可能有大量的数据，从上面的测试结果看，100W 条记录也只消耗了 20 多秒，1秒钟大约有 4W-5W 条数据，尽管上述插入的数据简单，但却说明了数据丢失的大量性。**更好的插入数据的做法是将值设置为****1****，然后修改存储过程，将每次循环都提交修改为只提交一次****，**这样既能保证数据的一致性，也能提升性能，修改如下：

```sql
drop procedure if exists proc;
delimiter $$
create procedure proc(i int)
begin
    declare s int default 1;
    declare c char(50) default repeat('a',50);
    start transaction;
    while s<=i DO
        insert into test_flush_log values(null,c);
        set s=s+1;
    end while;
    commit;
end$$
delimiter ;
```

测试值为1时的情况。

```sql
mysql> set @@global.innodb_flush_log_at_trx_commit=1;
mysql> truncate test_flush_log;

mysql> call proc(1000000);
Query OK, 0 rows affected (11.26 sec)
```

### 日志块（log block）

innodb 存储引擎中，redo log 以块为单位进行存储的，每个块占 512 字节，这称为 redo log block。所以不管是 log buffer 中还是 os buffer 中以及 redo log file on disk 中，都是这样以 512字节的块存储的。

每个redo log block 由3部分组成：**日志块头、日志块尾和日志主体**。其中日志块头占用 12 字节，日志块尾占用  8 字节，所以每个 redo log block 的日志主体部分只有 512-12-8=492 字节。

![img](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508182701906-2079813573.png)

因为 redo log 记录的是数据页的变化，当一个数据页产生的变化需要使用超过 492 字节()的 redo log 来记录，那么就会使用多个 redo log block 来记录该数据页的变化。

日志块头包含 4 部分：

* log_block_hdr_no：(4字节)该日志块在redo log buffer中的位置ID。

* log_block_hdr_data_len：(2字节)该log block中已记录的log大小。写满该log block时为0x200，表示512字节。

* log_block_first_rec_group：(2字节)该log block中第一个log的开始偏移位置。

* lock_block_checkpoint_no：(4字节)写入检查点信息的位置。

关于log block块头的第三部分 log_block_first_rec_group ，因为有时候一个数据页产生的日志量超出了一个日志块，这是需要用多个日志块来记录该页的相关日志。例如，某一数据页产生了552字节的日志量，那么需要占用两个日志块，第一个日志块占用492字节，第二个日志块需要占用60个字节，那么对于第二个日志块来说，它的第一个log的开始位置就是73字节(60+12)。如果该部分的值和 log_block_hdr_data_len 相等，则说明该log block中没有新开始的日志块，即表示该日志块用来延续前一个日志块。

日志尾只有一个部分： log_block_trl_no ，该值和块头的 log_block_hdr_no 相等。

上面所说的是一个日志块的内容，在redo log buffer或者redo log file on disk中，由很多log block组成。如下图：

![img](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508182756285-1761418702.png)

### log group 和 redo log file

log group 表示的是 redo log group，一个组内由多个大小完全相同的 redo log file 组成。组内redo log file的数量由变量` innodb_log_files_group` 决定，默认值为 2，即两个 redo log file。这个组是一个逻辑的概念，并没有真正的文件来表示这是一个组，但是可以通过变量 `innodb_log_group_home_dir` 来定义组的目录，redo log file 都放在这个目录下，默认是在datadir 下。

```sql
mysql> show global variables like "innodb_log%";
+-----------------------------+----------+
| Variable_name               | Value    |
+-----------------------------+----------+
| innodb_log_buffer_size      | 8388608  |
| innodb_log_compressed_pages | ON       |
| innodb_log_file_size        | 50331648 |
| innodb_log_files_in_group   | 2        |
| innodb_log_group_home_dir   | ./       |
+-----------------------------+----------+

[root@xuexi data]# ll /mydata/data/ib*
-rw-rw---- 1 mysql mysql 79691776 Mar 30 23:12 /mydata/data/ibdata1
-rw-rw---- 1 mysql mysql 50331648 Mar 30 23:12 /mydata/data/ib_logfile0
-rw-rw---- 1 mysql mysql 50331648 Mar 30 23:12 /mydata/data/ib_logfile1
```

可以看到在默认的数据目录下，有两个 ib_logfile 开头的文件，它们就是 log group 中的 redo log file，而且它们的大小完全一致且等于变量` innodb_log_file_size` 定义的值。第一个文件ibdata1是在没有开启 innodb_file_per_table 时的共享表空间文件，对应于开启 innodb_file_per_table 时的.ibd文件。

在 innodb 将 log buffer 中的 redo log block 刷到这些 log file 中时，会以追加写入的方式循环轮训写入。即先在第一个 log file（即 ib_logfile0）的尾部追加写，直到满了之后向第二个 log file（即ib_logfile1）写。当第二个 log file 满了会清空一部分第一个 log file继续写入。

由于是将log buffer中的日志刷到log file，所以在log file中记录日志的方式也是log block的方式。

在每个组的第一个redo log file中，前2KB记录4个特定的部分，从2KB之后才开始记录log block。除了第一个redo log file中会记录，log group中的其他log file不会记录这2KB，但是却会腾出这2KB的空间。如下：

![img](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508183757511-1174307952.png)

redo log file 的大小对 innodb 的性能影响非常大，设置的太大，恢复的时候就会时间较长，设置的太小，就会导致在写 redo log 的时候循环切换 redo log file。

### redo log 的格式

因为 innodb 存储引擎存储数据的单元是页，所以 redo log 也是基于页的格式来记录的。默认情况下，innodb 的页大小是16KB(由 innodb_page_size 变量控制)，一个页内可以存放非常多的 log block (每个512字节)，而 log block 中记录的又是数据页的变化。

其中 log block 中492字节的部分是 log body，该 log body 的格式分为4部分：

- redo_log_type：占用1个字节，表示redo log的日志类型。
- space：表示表空间的ID，采用压缩的方式后，占用的空间可能小于4字节。
- page_no：表示页的偏移量，同样是压缩过的。
- redo_log_body表示每个重做日志的数据部分，恢复时会调用相应的函数进行解析。例如insert语句和delete语句写入redo log的内容是不一样的。

如下图，分别是insert和delete大致的记录方式。![img](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508184303598-1449455496.png)

### 日志刷盘的规则

log buffer中未刷到磁盘的日志称为脏日志(dirty log)。

在上面的说过，默认情况下事务每次提交的时候都会刷事务日志到磁盘中，这是因为变量 `innodb_flush_log_at_trx_commit` 的值为1。但是innodb不仅仅只会在有commit动作后才会刷日志到磁盘，这只是innodb存储引擎刷日志的规则之一。

刷日志到磁盘有以下几种规则：

**1.发出commit动作时。已经说明过，commit发出后是否刷日志由变量 innodb_flush_log_at_trx_commit 控制。**

**2.每秒刷一次。这个刷日志的频率由变量 innodb_flush_log_at_timeout 值决定，默认是1秒。要注意，这个刷日志频率和commit动作无关。**

**3.当log buffer中已经使用的内存超过一半时。**

**4.当有checkpoint时，checkpoint在一定程度上代表了刷到磁盘时日志所处的LSN位置。**

### 数据页刷盘的规则及checkpoint

内存中(buffer pool)未刷到磁盘的数据称为脏数据(dirty data)。由于数据和日志都以页的形式存在，所以脏页表示脏数据和脏日志。

上一节介绍了日志是何时刷到磁盘的，不仅仅是日志需要刷盘，脏数据页也一样需要刷盘。

**在innodb中，数据刷盘的规则只有一个：checkpoint。**但是触发checkpoint的情况却有几种。

**不管怎样，checkpoint 触发后，会将 buffer 中脏数据页和脏日志页都刷到磁盘**

innodb存储引擎中checkpoint分为两种：

- sharp checkpoint：在重用redo log文件(例如切换日志文件)的时候，将所有已记录到redo log中对应的脏数据刷到磁盘。
- fuzzy checkpoint：一次只刷一小部分的日志到磁盘，而非将所有脏日志刷盘。有以下几种情况会触发该检查点：
  - master thread checkpoint：由master线程控制，**每秒或每10秒**刷入一定比例的脏页到磁盘。
  - flush_lru_list checkpoint：从MySQL5.6开始可通过 innodb_page_cleaners 变量指定专门负责脏页刷盘的page cleaner线程的个数，该线程的目的是为了保证lru列表有可用的空闲页。
  - async/sync flush checkpoint：同步刷盘还是异步刷盘。例如还有非常多的脏页没刷到磁盘(非常多是多少，有比例控制)，这时候会选择同步刷到磁盘，但这很少出现；如果脏页不是很多，可以选择异步刷到磁盘，如果脏页很少，可以暂时不刷脏页到磁盘
  - dirty page too much checkpoint：脏页太多时强制触发检查点，目的是为了保证缓存有足够的空闲空间。too much的比例由变量 innodb_max_dirty_pages_pct 控制，MySQL 5.6默认的值为75，即当脏页占缓冲池的百分之75后，就强制刷一部分脏页到磁盘。

由于刷脏页需要一定的时间来完成，所以记录检查点的位置是在每次刷盘结束之后才在redo log中标记的。

> MySQL停止时是否将脏数据和脏日志刷入磁盘，由变量innodb_fast_shutdown={ 0|1|2 }控制，默认值为1，即停止时只做一部分purge，忽略大多数flush操作(但至少会刷日志)，在下次启动的时候再flush剩余的内容，实现fast shutdown。

### Innodb 的恢复行为

在启动innodb的时候，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。

因为redo log记录的是数据页的物理变化，因此恢复的时候速度比逻辑日志(如二进制日志)要快很多。而且，innodb自身也做了一定程度的优化，让恢复速度变得更快。

重启innodb时，checkpoint表示已经完整刷到磁盘上data page上的LSN，因此恢复时仅需要恢复从checkpoint开始的日志部分。例如，当数据库在上一次checkpoint的LSN为10000时宕机，且事务是已经提交过的状态。启动数据库时会检查磁盘中数据页的LSN，如果数据页的LSN小于日志中的LSN，则会从检查点开始恢复。

还有一种情况，在宕机前正处于checkpoint的刷盘过程，且数据页的刷盘进度超过了日志页的刷盘进度。这时候一宕机，数据页中记录的LSN就会大于日志页中的LSN，在重启的恢复过程中会检查到这一情况，这时超出日志进度的部分将不会重做，因为这本身就表示已经做过的事情，无需再重做。

另外，事务日志具有幂等性，所以多次操作得到同一结果的行为在日志中只记录一次。而二进制日志不具有幂等性，多次操作会全部记录下来，在恢复的时候会多次执行二进制日志中的记录，速度就慢得多。例如，某记录中id初始值为2，通过update将值设置为了3，后来又设置成了2，在事务日志中记录的将是无变化的页，根本无需恢复；而二进制会记录下两次update操作，恢复时也将执行这两次update操作，速度比事务日志恢复更慢。

### 和 redo log有关的几个变量

- innodb_flush_log_at_trx_commit={0|1|2} # 指定何时将事务日志刷到磁盘，默认为1。
  - 0表示每秒将"log buffer"同步到"os buffer"且从"os buffer"刷到磁盘日志文件中。
  - 1表示每事务提交都将"log buffer"同步到"os buffer"且从"os buffer"刷到磁盘日志文件中。
  - 2表示每事务提交都将"log buffer"同步到"os buffer"但每秒才从"os buffer"刷到磁盘日志文件中。
- innodb_log_buffer_size：# log buffer的大小，默认8M
- innodb_log_file_size：#事务日志的大小，默认5M
- innodb_log_files_group =2：# 事务日志组中的事务日志文件个数，默认2个
- innodb_log_group_home_dir =./：# 事务日志组路径，当前目录表示数据目录
- innodb_mirrored_log_groups =1：# 指定事务日志组的镜像组个数，但镜像功能好像是强制关闭的，所以只有一个log group。在MySQL5.7中该变量已经移除。