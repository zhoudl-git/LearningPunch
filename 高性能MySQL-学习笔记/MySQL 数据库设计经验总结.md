原文 | http://t.cn/EX58dqC

作者 | Lucifer_Yu

# 背景

此文仅在数据库设计层面进行探讨，数据库的运维与底层调优不在讨论范围之内。

更丰富的知识可以在 [MySQL 官网文档](https://docs.oracle.com/cd/E17952_01/mysql-5.7-en/index.html) 查阅。

学习官方文档也是一种好的习惯，能更系统更全面的掌握某一领域的知识，具体知识点也可以通过搜索引擎快速获取，但是很难让你深入到细节或者上升到宏观层面。

# 基础知识

## 存储引擎

- 通常来说，我们做业务开发，指定存储引擎为 InnoDB 即可。

## 字符集

- 通常来说，只要指定为 utf8 或者 utf8mb4 即可。
- 如果业务中需要使用 emoji 表情，那么就必须要设置为 utf8mb4

MySQL 可以在 Server 级、Database 级、Table 级、Column 级进行字符集的设置。

# 数据库设计

## 总则

- 命名以“_”分割_

数据库层面还是推荐使用“_”作为分割，这里多说几点：
1、约定俗成。长久以来不仅  MySQL ，其他数据库也推荐使用 “_”，这是一种 SQL 规范。
2、JSON 返回的数据一般也会将驼峰转化为“_”来分割。
3、JAVA POJO 对象还是使用驼峰命名，现在的 JSON 转换工具， ORM 工具可以很便捷的指定参数来设置驼峰或者下划线的偏好。

如果仅是使用 Mybatis ，为了减少配置，也可以考虑使用驼峰命名数据库字段，或者使用 Mybatis 的 `mapUnderscoreToCamelCase` 的参数来解决。

mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <setting name="mapUnderscoreToCamelCase" value="true" />
    </settings>
</configuration>
```
注意：`resultType="hashMap"` 的时候，不会生效，一定要有对应的实体。
Spring Boot 中指定 `mybatis.configuration.mapUnderscoreToCamelCase=true`

- 综合考虑应用场景，合理分库分表
- 合理冗余字段，合理参考范式，以空间换时间
- 尽量保证数据库单表操作，减少关联查询
- 合理使用索引
- 合理设计所有字段及表关联
- 尽量避免在数据库层面实现逻辑

* 尽量避免使用触发器，视图，储存过程，将业务逻辑与统计逻辑分离

* 不要在业务库中大量统计计算

* 尽量避免使用各种数据库函数

## 库设计

- 不允许物理删除，需要逻辑删除的使用 is_deleted 字段。除非是一些无关紧要的关联关系变更，可以物理删除再插入，然后通过操作日志进行追踪。
- 日志表、关联关系表等固定数据表，需要包含 create_by， create_at 三个字段，业务表必须带 create_by，create_at，update_by，update_at 四个字段。如果经常查询，需要展示创建者修改者名称，可以增加 create_name 和 update_name 字段来进行冗余，且应该使用真实姓名，name 仅做展示用，id 字段便于数据统计。
- 流水表是不可变更表，不需要带 is_deleted，create_by 等字段。
- 业务表状态简单的只要使用 status 字段即可，如果状态复杂且要对不同状态分组，可以考虑使用 status_group 之类的额外字段来进行简化。

## 表设计

- 不建议以 “t_” 开头

一般来说以“t_”开头是为了表示这是一张表，区别于视图，触发器，存储过程等。而现在互联网架构一般不建议使用视图，触发器，存储过程等。而且很多企业只有一个库，全是 t_ 开头的数十上百张表，根本不知道怎么快速检索。

- 直接使用业务对象为表名 order，也不建议使用复数形式

如果已经按照功能模块做了数据库的拆分，可以不要使用任何前缀，直接使用表名，如 orders 。如果还在使用一个库放所有的表的话，那就最好已模块名开头，比较客服系统的工单表，可以命名为 cs_jobs 。

注意：不要与数据库关键字冲突

- 关系表以 “_rel” 结尾（ rel 这个结尾不一定很合适，有更好的可以推荐）
- 日志表以 “_log” 结尾
- 表名要简约（这一点可能也挺难，尽量避免拼音作为表名，英文可以适当采取缩写，比如 rel-关联关系 或者 corp-公司，但是也要避免滥用缩写，或者自己创造缩写 ）

## 字段设计

- 每张表都应该有独立的 id 字段，不管是自增字段还是自己制定的 id（uuid 及其他类型的唯一主键都可以，如某宝的订单号和支付 ID ）

扩展知识点：第二范式 聚簇索引 非聚簇索引

- 所有字段非空
- 注意整型的长度修饰并不代表字段存储值的范围，只是展示长度。

这一点很多开发人员都没有注意过。我们还是看一下官网的描述。[11.1.1 Numeric Type Overview](https://docs.oracle.com/cd/E17952_01/mysql-5.7-en/numeric-type-overview.html) int(11) 中的 11 只是展示位数，并不影响实际存储的值，配合 ZEROFILL ,改变的只是查询的值，比如数据库字段定义为 INT(4)，值为 1，启用了 ZEROFILL， SELECT 的结果展示为 0001。

![img](https://oscimg.oschina.net/oscnet/a383aef74975f42e93826b41cea516c930e.jpg)

> INT[(M)] [UNSIGNED] [ZEROFILL]
>
> A normal-size integer. The signed range is -2147483648 to 2147483647. The unsigned range is 0 to 4294967295.
>
> M indicates the maximum display width for integer types. The maximum display width is 255. Display width is unrelated to the range of values a type can contain, as described in Section 11.2, “Numeric Types”. For floating-point and fixed-point types, M is the total number of digits that can be stored.
>
> MySQL supports the SQL standard integer types INTEGER (or INT) and SMALLINT. As an extension to the standard, MySQL also supports the integer types TINYINT, MEDIUMINT, and BIGINT.
>

如果要考虑数据迁移的话，尽量使用 INT 或者 SMALLINT。

- 整型合理使用 UNSIGNED

整型的 UNSIGNED很魔性，需要结合实际场景使用。ZEROFILL 默认使用 UNSIGNED。UNSIGNED 修饰有两大作用：一是保证列为非负数，二是可以扩大使用范围，个人推荐使用 UNSIGNED。

- 确保每列的原子性，每个字段的含义应该唯一（第一范式）

不能用一个字段来表示两种逻辑含义，还有就是字段要存储直接的字面量，不要存储需要计算的值，比如使用类 Linux 文件系统权限的模式来处理状态

- 表中不能存储文件，只能存放 url

如果使用了分布式文件储存系统，或者用了第三方的文件存储服务。可以不用存储域名前缀，应用层实现拼接或者前端处理。

- 尽量避免使用 text 或 blog 字段

确实需要存储的可以考虑拆表，将基本字段与扩展大字段分开。甚至可以考虑采用 MongoDB 之类的数据库才存储大量的文本表。

## 索引设计

- 尽量使用单列索引，避免使用联合索引，不要建 3 列以上的联合索引。
- 单表的索引数量不要过多，控制在6个以内，索引的建立和更新也需要时间，可以关注所以占用的存储空间，多列索引还要注意保持顺序才能生效，多列索引也要考虑每个字段的检索效率，效率高的字段在前。

索引失效的几种情况：
1、字段值的区分度太小，比如性别的 0，1
2、like 使用了前模糊匹配 like '%asdf'
3、使用了函数 date(birthday) 或者进行了计算 a + 50
4、违反左匹配原则 如联合索引 a, b 实际查询使用顺序为 b, a
5、索引列中含有 NULL



