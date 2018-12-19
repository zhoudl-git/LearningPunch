### Undo Log

#### 概念

undo log 指事务开始之前，才操作任何数据之前，首先将需操作的数据备份到一个地方（也就是 undo log）

undo log 是为了实现事务的原子性而出现的产物

undo log 在 innodb引擎中用来实现多版本并发控制

#### 当前读

#### 快照读

SQL读取的数据是快照版本，也就是历史版本，普通的select就是快照读

### Redo Log

