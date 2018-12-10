## CenterOS下MySQL常用命令总结

### 连接Mysql

```
mysql -h 主机地址 -u用户名 -p用户密码

连接本机：
mysql -uroot -p12345 ;登录
连接远程：
mysql -h 192.168.0.115 -uroot -p12345
```

### 退出

```
exit;
```

### 修改密码

```sql
mysqladmin -u用户名 -p旧密码 password 新密码

给root加个密码ab12
mysqladmin -uroot -password ab12
注：因为开始时root没有密码，所以-p旧密码一项就可以省略了
再将root的密码改为djg345
mysqladmin -uroot -pab12 password djg345
```

(注意：和上面不同，下面的因为是MySQL环境中的命令，所以后面都带一个分号作为命令结束符)

### 增加新用户

```
grant select on 数据库.* to 用户名@登录主机 identified by \"密码\"
```

### 创建用户并授权

```
grant 允许操作 on 库名.表名 to 账号@来源 identified by '密码';

创建zhangsan账号，密码123，授权test库下所有表的增/删/改/查数据,来源地不限
grant select,insert,update,delete on test.* to zhangsan@'%' identified by '123';
```

### 取消授权

```
revoke all on *.* from root@”%”;
注意：
revoke 跟 grant 的语法差不多，只需要把关键字 “to” 换成 “from” 即可

由于收回授权本质上是在数据库中删除一条数据 从而上边的授权语句亦可以换成一条删除语句
delete from user where user=”root” and host=”%”;
```

### 建表

```sql
create table table_name (column_name column_type);

如创建表：
CREATE TABLE IF NOT EXISTS `runoob_tbl`(
   `runoob_id` INT UNSIGNED AUTO_INCREMENT,
   `runoob_title` VARCHAR(100) NOT NULL,
   `runoob_author` VARCHAR(40) NOT NULL,
   `submission_date` DATE,
   PRIMARY KEY ( `runoob_id` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

* 如果你不想字段为 NULL 可以设置字段的属性为 NOT NULL， 在操作数据库时如果输入该字段的数据为NULL ，就会报错。
* AUTO_INCREMENT定义列为自增的属性，一般用于主键，数值会自动加1。
* PRIMARY KEY关键字用于定义列为主键。 您可以使用多列来定义主键，列间以逗号分隔。
* ENGINE 设置存储引擎，CHARSET 设置编码。

### 插入数据

```sql
INSERT INTO table_name 
( field1, field2,...fieldN )
VALUES
( value1, value2,...valueN );

如：
 INSERT INTO runoob_tbl 
    -> (runoob_title, runoob_author, submission_date)
    -> VALUES
    -> ("学习", "zhoudl", NOW());
```

### 修改列类型

```sql
修改列id的类型为int unsigned
alter table table1 modify id int unsigned;

修改列id的名字为sid，而且把属性修改为int unsigned
alter table table1 change id sid int unsigned;
```

### 创建索引

```sql
alter table table1 add index ind_id (id);

create index ind_id on table1 (id);

建立唯一性索引
create unique index ind_id on table1 (id);
```

### 删除索引

```sql
drop index idx_id on table1;

alter table table1 drop index ind_id;
```

### 分页

```sql
limit(选出10到20条)<第一个记录集的编号是0>

select * from students order by id limit 9,10;
```

### 其他常用命令

```sql
create database name;创建数据库
user databasename;选择数据库
drop databasename;直接删除数据库，不提醒
mysqladmin drop databasename;删除数据前，有提示
show tables;显示所有表
describe tablename;表的详细描述
select version();显示当前mysql版本
select current_data;查看当前日期
select user();查看当前用户
show processlist;查看当前数据库队列

flush privileges;刷新数据库
use dbname;打开数据库
show databases;显示所有数据库
show tables;显示数据库中的所有表
describe user;显示表user的详细信息

联合字符或者多个列(将列id与":"和列name和"="连接)
select concat(id,':',name,'=') from students;
```

### 数据库管理

```sql
备份数据库
mysqldump -h host -u root -p dbname >dbname_backup.sql；
恢复数据库
mysqladmin -h myhost -u root -p create dbname
mysqldump -h host -u root -p dbname < dbname_backup.sql

```

