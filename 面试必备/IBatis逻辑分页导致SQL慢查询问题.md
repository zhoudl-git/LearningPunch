## 后台管理系统采用IBatis逻辑分页导致SQL慢查询问题
`ibatis`一词来源于`internet`和`abatis`的组合，是一个由`Clinton Begin`在`2001`年发起的开放源代码项目。于2010 年 6 月 16 号被谷歌托管，改名为`MyBatis`。是一个基于`SQL`映射支持`Java`和`.NET`的持久层框架。

### ibatis优点
#### 半自动化
“半自动化”的`ibatis`，却刚好解决了这个问题。这里的“半自动化”，是相对`Hibernate`等提供了全面的数据库封装机制的“全自动化”`ORM `实现而言，“全自动”`ORM `实现了 `POJO` 和数据库表之间的映射，以及 `SQL` 的自动生成和执行。而`ibatis` 的着力点，则在于`POJO` 与 `SQL`之间的映射关系。也就是说，`ibatis`并不会为程序员在运行期自动生成 `SQL` 执行。具体的 `SQL `需要程序员编写，然后通过映射配置文件，将`SQL`所需的参数，以及返回的结果字段映射到指定` POJO`。
通常在如下场景和条件下，选择`ibatis`, 将更有助于发挥`ibatis`在持久层的优越性：
1. 知道怎样操作`10`种以上的数据库
2. 可配置的`caching`(包括从属)
3. 支持`DataSource`、`local transaction management`和`global transaction`
4. 简单的`XML`配置文档
5. 支持`Map`, `Collection`, `List`和简单类型包装(如`Integer`, `String`)
6. 支持`JavaBeans`类(`get/set` 方法)
7. 支持复杂的对象映射(如`populating lists`, `complex object models`)
8. 对象模型从不完美(不需要修改)
9. 数据模型从不完美(不需要修改)
10. 你已经知道`SQL`，为什么还要学习其他东西

#### 全自动化
使用`ibatis`提供的`ORM`机制，对业务逻辑实现人员而言，面对的是纯粹的 `Java`对象，
这一层与通过`Hibernate` 实现 `ORM` 而言基本一致，而对于具体的数据操作，`Hibernate`
会自动生成`SQL` 语句，而ibatis 则要求开发者编写具体的`SQL` 语句。相对`Hibernate`等
“全自动”`ORM`机制而言，`ibatis `以 `SQL`开发的工作量大和数据库移植性上差为代价，为系统
设计提供了更大的自由空间。作为“全自动”`ORM`实现的一种有益补充，`ibatis` 的出现显
得别具意义。

### ibatis不足
```java
public class SqlMapClientImpl implements SqlMapClient, ExtendedSqlMapClient {

// 查询对象方法
 public Object queryForObject(String id, Object paramObject, Object resultObject) throws SQLException {
    return getLocalSqlMapSession().queryForObject(id, paramObject, resultObject);
  }
// 查询列表方法
  public List queryForList(String id, Object paramObject) throws SQLException {
    return getLocalSqlMapSession().queryForList(id, paramObject);
  }
}
```
#### 实际调用链路
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190403185811767.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NoYW5nX3hz,size_16,color_FFFFFF,t_70)
#### 执行SQL拼接和调用执行
```java
// MappedStatement#executeQueryForObject执行MappedStatement
public Object executeQueryForObject(StatementScope statementScope, Transaction trans, Object parameterObject, Object resultObject)
      throws SQLException {
    try {
      Object object = null;

      DefaultRowHandler rowHandler = new DefaultRowHandler();
      // 实际执行调用方法
      executeQueryWithCallback(statementScope, trans.getConnection(), parameterObject, resultObject, rowHandler, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
	...
    } catch (TransactionException e) {
    ...
    }
  }
```
#### 调用SQL执行
```java
protected void executeQueryWithCallback(StatementScope statementScope, Connection conn, Object parameterObject, Object resultObject, RowHandler rowHandler, int skipResults, int maxResults)
     throws SQLException {
   try {
   ...
    // 校验参数
     parameterObject = validateParameter(parameterObject);
    // 获取SQL
     Sql sql = getSql();
    // 获取parameterMap 
     ParameterMap parameterMap = sql.getParameterMap(statementScope, parameterObject);
    // 执行SQL调用
     sqlExecuteQuery(statementScope, conn, sqlString, parameters, skipResults, maxResults, callback);
   } catch (SQLException e) {
    ...
 }
```
#### 结果集映射
```java
private ResultSet handleMultipleResults(PreparedStatement ps, StatementScope statementScope, int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
    ResultSet rs;
    // 获取调用结果
    rs = getFirstResultSet(statementScope, ps);
    if (rs != null) {
    // 处理结果集
      handleResults(statementScope, rs, skipResults, maxResults, callback);
    }
	...
    return rs;
  }
```
```java
private void handleResults(StatementScope statementScope, ResultSet rs, int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
   try {
     statementScope.setResultSet(rs);
     ResultMap resultMap = statementScope.getResultMap();
     if (resultMap != null) {
       // 跳过处理部分结果
       if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
         if (skipResults > 0) {
           rs.absolute(skipResults);
         }
       } else {
         for (int i = 0; i < skipResults; i++) {
           if (!rs.next()) {
             return;
           }
         }
       }
       // 获取最终结果集
       int resultsFetched = 0;
       while ((maxResults == SqlExecutor.NO_MAXIMUM_RESULTS || resultsFetched < maxResults) && rs.next()) {
         Object[] columnValues = resultMap.resolveSubMap(statementScope, rs).getResults(statementScope, rs);
         callback.handleResultObject(statementScope, columnValues, rs);
         resultsFetched++;
       }
     }
   } finally {
     statementScope.setResultSet(null);
   }
 }
```
#### ibatis存在的逻辑分页问题

- 从代码中可以看出`ibatis`分页查询的逻辑是首先判断`ResulteSet`的类型，如果`ResultSet`的类型是`ResultSet.TYPE_FORWARD_ONLY`，则使用ResultSet对象的`next()`方法，一步一步地移动游标到要取的第一条记录的位置，然后再采用`next()`方法取出一页的数据；如果`ResultSet`的类型不是`ResultSet.TYPE_FORWARD_ONLY`，则采用 ResultSet对象的`absolute()`方法，移动游标到要取的第一条记录的位置，然后再采用`next()`方法取出一页的数据。

- 其中`resultSetType`的可选值为`FORWARD_ONLY | SCROLL_INSENSITIVE | SCROLL_SENSITIVE`，如果没有配置，默认值为`FORWARD_ONLY`，`FORWARD_ONLY`类型的`ResultSet `不支持`absolute`方法，所以是通过next方法定位的。一般情况下，我们都使用`FORWARD_ONLY`类型的`ResultSet`，`SCROLL`类 型`ResultSet`的优点是可向前，向后滚动，并支持精确定位（`absolute`）,但缺点是把结果集全部加载进缓存（如果查询是从`1000000`条开 始取`100`条，会把前`100`万条数据也加载进缓存），容易造成内存溢出，性能也很差，除非必要，一般不使用。

- 由于，`ibatis`的分页完全依赖于`JDBC ResultSet`的`next`方法或`absolute`方法来实现。
所以分页还是要考虑采用直接操作`sql`语句来完成。当然，小批量的可以采用`ibatis`的分页模式。一般分页的`sql`语句与数据库的具体实现有关。
## 在框架基础上实现物理分页
### 需求分析
框架自身问题有两个：
1. 在于分页实现是在结果集返回之后，所以我们面对的问题是，在`SQL`执行之前实现分页`SQL`的分页拼接。
2. 在执行器中执行后，告诉结果集处理器不在进行逻辑分页处理，直接采用`SQL`查询结果，作为最终的结果集。

### 代码分析
1. 分析代码可知主要执行器`com.ibatis.sqlmap.engine.execution.SqlExecutor`，由于没有采用接口实现的方式，所以实现接口是不可能的
2. 如果想要完成`SQL`拦截，可以有两种方式，采用拦截器，动态代理；或者采用反射方式实现自定义处理器的注入

### 代码实现
#### 自定义Executor
```java
/**
 * @ClassName: ExtSqlExecutor
 * @Description: 自定义处理器
 * @Author: 尚先生
 * @CreateDate: 2019/4/3 19:28
 * @Version: 1.0
 */
@Component("extSqlExecutor")
public class ExtSqlExecutor extends SqlExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ExtSqlExecutor.class);

    private static final String SQL_END_DELIMITER = ";";

    /**
     * 不跳过结果
     */
    public static final int NO_SKIPPED_RESULTS = 0;
    /**
     * 查询所有结果
     */
    public static final int NO_MAXIMUM_RESULTS = -999999;

    public void executeQuery(StatementScope statementScope, Connection conn, String sql, Object[] parameters, int skipResults, int maxResults, RowHandlerCallback callback) throws SQLException {
        if (skipResults != NO_SKIPPED_RESULTS || maxResults != NO_MAXIMUM_RESULTS){
            sql = getLimitSql(sql,skipResults,maxResults);
        }
        logger.info("自定义执行器，查询SQL：", new Object[]{sql});

    }

    /**
     * 拼接SQL
     * @param sql
     * @param offset
     * @param limit
     * @return
     */
    public String getLimitSql(String sql, int offset, int limit) {
        sql = trim(sql);
        StringBuffer sb = new StringBuffer(sql.length() + 20);
        sb.append(sql);
        if (offset > 0) {
            sb.append(" limit ").append(offset).append(',').append(limit)
                    .append(SQL_END_DELIMITER);
        } else {
            sb.append(" limit ").append(limit).append(SQL_END_DELIMITER);
        }
        return sb.toString();
    }

    /**
     * 根据结束符号截取SQL
     * @param sql
     * @return
     */
    private String trim(String sql) {
        sql = sql.trim();
        if (sql.endsWith(SQL_END_DELIMITER)) {
            sql = sql.substring(0, sql.length() - 1
                    - SQL_END_DELIMITER.length());
        }
        return sql;
    }
}
```
#### 反射设置自定义处理器
```java
/**
 * @ClassName: ReflectUtils
 * @Description: 反射设置自定义处理器
 * @Author: 尚先生
 * @CreateDate: 2019/4/3 19:38
 * @Version: 1.0
 */
public class ReflectUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    /**
     * 执行 set方法
     * @param target
     * @param name
     * @param type
     * @param value
     */
    public static void setFieldValue(Object target, String name, Class type,
                                     Object value) {
        if (target == null || name == null || StringUtils.isEmpty(name) || StringUtils.isEmpty(name)
        || (value != null && !type.isAssignableFrom(value.getClass()))){

            logger.error("设置自定义处理器异常，原因是存在参数值为空");
            return;
        }
        Class clazz = target.getClass();
        try {
            Field field = clazz.getDeclaredField(name);
            if (!Modifier.isPublic(field.getModifiers())) {
                // 设置属性可获取
                field.setAccessible(true);
            }
            field.set(target, value);
        } catch (Exception e) {
            logger.error("设置自定义处理器异常，异常信息：" ,new Object[]{e});
        }
    }
}
```

#### 封装数据库执行类
```java
/**
 * @ClassName: BaseDao
 * @Description: 封装数据库执行类
 * @Author: 尚先生
 * @CreateDate: 2019/4/3 19:41
 * @Version: 1.0
 */
public class BaseDao {

    @Autowired
    private SqlMapClient sqlMapClient;

    @Autowired
    private DataSource dataSource;

    @Autowired
    @Qualifier("extSqlExecutor")
    private SqlExecutor sqlExecutor;

    // 容器启动完成，执行设置自定义executor
    @PostConstruct
    public void initalizeExtexecutor(){
        if (null != sqlExecutor){
            if (sqlMapClient instanceof SqlMapClientImpl){
                SqlMapClientImpl client = (SqlMapClientImpl) this.sqlMapClient;
                ReflectUtils.setFieldValue(client.getDelegate(), "sqlExecutor",SqlExecutor.class,sqlExecutor);
            }
        }
    }
}
// 整合 SqlSession、DataSource
...
```
## 执行结果分析
预制测试环境`100000`条数据，分页查询`200`条数据，
```sql
select * from tb_cust order by create_time desc limit 90000, 200 ;
```

### 改造前
```tex
cost:35261ms
```
### 改造后
```tex
cost:1087ms
```
### 总结
实现物理分页之后由于节省查询结果内存，在有大量数据关联查询或者排序的情况下，效果会十分明显

## MyBatis 相关文章推荐
在之前分享的文章中有许多关于`MyBatis `的深入使用及扩展，详情可参考如下：
>MyBatis深入理解和使用-MyBatis缓存体系:https://blog.csdn.net/shang_xs/article/details/86656353
>MyBatis深入理解和使用-MyBatis事务管理:https://blog.csdn.net/shang_xs/article/details/86656649
>MyBatis深入理解和使用-TypeHandler:https://blog.csdn.net/shang_xs/article/details/86656173