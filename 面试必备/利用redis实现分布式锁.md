作者：燕少༒江湖 
原文：https://blog.csdn.net/qq_31289187/article/details/84399880 

### 引言

大多数互联网系统都是分布式部署的，分布式部署确实能带来性能和效率上的提升，但为此，我们就需要多解决一个分布式环境下，数据一致性的问题。

当某个资源在多系统之间，具有共享性的时候，为了保证大家访问这个资源数据是一致的，那么就必须要求在同一时刻只能被一个客户端处理，不能并发的执行，否者就会出现同一时刻有人写有人读，大家访问到的数据就不一致了。

### 我们为什么需要分布式锁？

在单机时代，虽然不需要分布式锁，但也面临过类似的问题，只不过在单机的情况下，如果有多个线程要同时访问某个共享资源的时候，我们可以采用线程间加锁的机制，即当某个线程获取到这个资源后，就立即对这个资源进行加锁，当使用完资源之后，再解锁，其它线程就可以接着使用了。例如，在JAVA中，甚至专门提供了一些处理锁机制的一些API（synchronize/Lock等）。

但是到了分布式系统的时代，这种线程之间的锁机制，就没作用了，系统可能会有多份并且部署在不同的机器上，这些资源已经不是在线程之间共享了，而是属于进程之间共享的资源。

因此，为了解决这个问题，我们就必须引入「分布式锁」。

分布式锁，是指在分布式的部署环境下，通过锁机制来让多客户端互斥的对共享资源进行访问。

分布式锁要满足哪些要求呢？

* 排他性：在同一时间只会有一个客户端能获取到锁，其它客户端无法同时获取

* 避免死锁：这把锁在一段有限的时间之后，一定会被释放（正常释放或异常释放）

* 高可用：获取或释放锁的机制必须高可用且性能佳

讲完了背景和理论，那我们接下来再看一下分布式锁的具体分类和实际运用。

### 分布式锁的实现方式有哪些？

目前主流的有三种，从实现的复杂度上来看，从上往下难度依次增加：

* 基于数据库实现

* 基于`Redis`实现

* 基于`ZooKeeper`实现

无论哪种方式，其实都不完美，依旧要根据咱们业务的实际场景来选择。

接下来着重介绍下基于redis的分布式锁实现

1. 分布锁一般通过`redis`实现，主要通过`setnx`函数向`redis`保存一个`key，value`等于保存时的时间戳，并设置过期时间，然后返回`true`；

2. 当获得锁超过等待时间返回`false`；

3. 通过`key`获取`redis`保存的时间戳，如果`value`不为空，并且当前时间戳减去`value`值超过锁过期时间返回`false`；

4. 如果一次没有获得锁，则每隔一定时间（`10ms`或者`20ms`）再获取一次，直到超过等待时间返回`false`。

### 具体实现

所需`jar`包如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
 
	<groupId>com.cn.dl</groupId>
	<artifactId>springboot-aop-test</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>
 
	<name>springboot-aop-test</name>
	<description>Demo project for Spring Boot</description>
 
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.0.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
 
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>
 
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
 
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-aop</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-expression</artifactId>
			<version>5.0.0.RELEASE</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
		</dependency>
		<dependency>
			<groupId>commons-lang</groupId>
			<artifactId>commons-lang</artifactId>
			<version>2.6</version>
		</dependency>
		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>fastjson</artifactId>
			<version>1.2.47</version>
		</dependency>
	</dependencies>
 
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
 
 
</project>

```

`RedisLockTestAnnotation`

```java
package com.cn.dl.annotation;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
/**
 * Created by yanshao on 2018/11/23.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLockTestAnnotation {
    String redisKey();
}
```

`RedisLockTestAspectUtils`

```java
package com.cn.dl.utils;
 
import com.cn.dl.annotation.RedisLockTestAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
 
 
/**
 * Created by yanshao on 2018/11/23.
 */
@Aspect
@Component
@Slf4j
public class RedisLockTestAspectUtils {
 
    @Autowired
    RedisLock redisLockUtil;
 
    @Around("@annotation(redisLock)")
    public Object redisLockTest(ProceedingJoinPoint point, RedisLockTestAnnotation redisLock){
 
        String lockKey = null;
        boolean flag = false;
        try {
            //根据
            String paramterIndex = redisLock.redisKey().substring(redisLock.redisKey().indexOf("#") + 1);
            int index = Integer.parseInt(paramterIndex);
            //获取添加注解方法中的参数列表
            Object[] args = point.getArgs();
            //生成redis的key：
            // TODO: 2018/11/23 根据固定为：REDIS_TEST_#数字，必须是参数列表对应的下表，从0开始，并且小于参数列表的长度
            lockKey = redisLock.redisKey().replace("#"+paramterIndex,args[index].toString());
            log.info("redis key:{}",lockKey);
            //set到redis
            flag = redisLockUtil.lock(lockKey,args[index].toString());
            log.info("redis save result:{}",flag);
            //执行添加了注解的方法并返回
            if(flag){
                Object result = point.proceed();
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        }finally {
            //最后在finally中删除
            if(flag){
                redisLockUtil.unlock(lockKey,"");
            }
        }
        return null;
    }
}
```

`RedisLock`

```
package com.cn.dl.utils;
 
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
 
import java.util.concurrent.TimeUnit;
 
/**
 * Created by yanshao on 2018/9/30.
 */
@Component
@Slf4j
public class RedisLock {
 
    @Autowired
    StringRedisTemplate redisTemplate;
 
    private static final long EXPIRE = 60 * 1000L;
 
    private static final long TIMEOUT = 10 * 1000L;
 
    public boolean lock(String key,String value){
        log.info("获取锁 kye:{},value:{}",key,value);
        //请求锁时间
        long requestTime = System.currentTimeMillis();
        while (true){
            //等待锁时间
            long watiTime = System.currentTimeMillis() - requestTime;
            //如果等待锁时间超过10s，加锁失败
            if(watiTime > TIMEOUT){
                log.info("等待锁超时 kye:{},value:{}",key,value);
                return false;
            }
 
            if(redisTemplate.opsForValue().setIfAbsent(key,String.valueOf(System.currentTimeMillis()))){
                //获取锁成功
                log.info("获取锁成功 kye:{},value:{}",key,value);
                //设置超时时间，防止解锁失败，导致死锁
                redisTemplate.expire(key,EXPIRE, TimeUnit.MILLISECONDS);
                return true;
            }
 
            String valueTime = redisTemplate.opsForValue().get(key);
            if(! StringUtils.isEmpty(valueTime) && System.currentTimeMillis() - Long.parseLong(valueTime) > EXPIRE){
                //加锁时间超过过期时间，删除key，防止死锁
                log.info("锁超时, key:{}, value:{}", key, value);
                try{
                    redisTemplate.opsForValue().getOperations().delete(key);
                }catch (Exception e){
                    log.info("删除锁异常 key:{}, value:{}", key, value);
                    e.printStackTrace();
                }
                return false;
            }
 
            //获取锁失败，等待20毫秒继续请求
            try {
                log.info("等待20 nanoSeconds key:{},value:{}",key,value);
                TimeUnit.NANOSECONDS.sleep(20);
            } catch (InterruptedException e) {
                log.info("等待20 nanoSeconds 异常 key:{},value:{}",key,value);
                e.printStackTrace();
            }
        }
    }
    /**
     * 分布式加锁
     * @param key
     * @param value
     * @return
     * */
    public boolean secKilllock(String key,String value){
        /**
         * setIfAbsent就是setnx
         * 将key设置值为value，如果key不存在，这种情况下等同SET命令。
         * 当key存在时，什么也不做。SETNX是”SET if Not eXists”的简写
         * */
        if(redisTemplate.opsForValue().setIfAbsent(key,value)){
            //加锁成功返回true
            return true;
        }
        String currentValue = redisTemplate.opsForValue().get(key);
 
        /**
         * 下面这几行代码的作用：
         * 1、防止死锁
         * 2、防止多线程抢锁
         * */
        if(! StringUtils.isEmpty(currentValue)
                && Long.parseLong(currentValue) < System.currentTimeMillis()){
            String oldValue = redisTemplate.opsForValue().getAndSet(key,value);
            if(! StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)){
                return true;
            }
        }
        return false;
    }
    /**
     * 解锁
     * @param key
     * @param value
     * */
    public void unlock(String key,String value){
        try{
            redisTemplate.opsForValue().getOperations().delete(key);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
```

`RedisConfig`

```java
package com.cn.dl.config;
 
/**
 * Created by yanshao on 2018/11/23.
 */
public interface RedisConfig {
 
    String REDIS_LOCK = "'REDIS_LOCK_'";
 
    String REDIS_TEST = "REDIS_TEST_";
 
    long REDIS_EXPIRE_TIME = 60L * 2;
 
}
```

`RestConfiguration`

> 加这个配置的原因是：保存到redis的key和value乱码问题，就是因为没有序列化！！！

```java
package com.cn.dl.config;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
 
/**
 * Created by yanshao on 2018/11/23.
 */
@Configuration
public class RestConfiguration {
 
    @Autowired
    private RedisTemplate redisTemplate;
 
    @Bean
    public RedisTemplate redisKeyValueSerializer() {
        //redis key和value值序列化，不序列话发现查到的key和value乱码
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
}
```

`StudentController`

```java
package com.cn.dl.controller;
 
import com.alibaba.fastjson.JSONObject;
import com.cn.dl.annotation.RedisLockTestAnnotation;
import com.cn.dl.config.RedisConfig;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
/**
 * Created by yanshao on 2018/11/23.
 */
@RestController
@RequestMapping("/student")
public class StudentController {
    @PostMapping("update")
    @RedisLockTestAnnotation(redisKey = RedisConfig.REDIS_TEST + "#0")
    public JSONObject sutdentInfoUpdate(@RequestParam("studentId") String studentId,
                                        @RequestParam("age") int age){
        JSONObject result = new JSONObject();
        result.put("update","success");
        return result;
    }
}
```

`application.properties`

```properties
server.port=7555
 
#redis
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=*****
```

### 总结

以上的实现方式，没有在所有场合都是完美的，包括文章刚开始提及的其他几种分布式锁实现方式，各有优缺点，所以，应根据不同的应用场景选择最适合的实现方式。

在分布式环境中，对资源进行上锁有时候是很重要的，比如抢购某一资源，这时候使用分布式锁就可以很好地控制资源。 当然，在具体使用中，还需要考虑很多因素，比如超时时间的选取，获取锁时间的选取对并发量都有很大的影响，上述实现的分布式锁也只是一种简单的实现，主要是一种思想，以上包括文中的代码，只做入门参考！

