> HTTP是一种无状态的协议，而服务器端的业务必须是要有状态的, 为了分辨链接是谁发起的，需自己去解决这个问题。不然有些情况下即使是同一个网站每打开一个页面或者 APP 每次进入也都要登录一下。
>
> 而 Session 、 Cookie、Token、JWT 等就是为解决这个问题而提出来的几个机制。

先欣赏一张一般的鉴权流程图

![图片来源网络 侵权联系删除](https:////upload-images.jianshu.io/upload_images/7066270-219fc75c768489a7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/905/format/webp)

根据上图可以看到，从用户请求发起，到服务端完成操作，流程颇多，但是HTTP无状态，我们如何才能详细记录这些操作过程并加以严格的权限判断控制，接下来就开始今天的主题！

### cookie

> Cookie 诞生的最初目的是为了存储 web中的状态信息，以方便服务器端使用。比如判断用户是否是第一次访问网站等问题。

1. Cookie 是客户端技术，程序把每个用户的数据以 cookie 的形式写给用户各自的浏览器。当用户使用浏览器再去访问服务器中的web资源时，就会带着各自的数据去。这样，web资源处理的就是用户各自的数据了。
2. cookie 的处理:

- 服务器像客户端发送 cookie
- 浏览器将 cookie 保存
- 之后每次 http 请求浏览器都会将 cookie 发送给服务器端

1. Java 提供的操作 Cookie 的 API

###### Java 中的 javax.servlet.http.Cookie 类用于创建一个 Cookie

1. `Cookie(String name,String value)`

   > 实例化Cookie对象，传入cooke名称和cookie的值

2. `public String getName()`

   > 取得Cookie的名字

3. `public String getValue()`

   > 取得Cookie的值

4. `public void setValue(String newvalue)`

   > 设置Cookie的值

5. `public void setMaxAge(int expiry)`

   > 设置Cookie的最大保存时间，即cookie的有效期，当服务器给浏览器回送一个cookie时，如果在服务器端没有调用setMaxAge方法设置cookie的有效期，那么cookie的有效期只在一次会话过程中有效，用户开一个浏览器，点击多个超链接，访问服务器多个web资源，然后关闭浏览器，整个过程称之为一次会话，当用户关闭浏览器，会话就结束了，此时cookie就会失效，如果在服务器端使用setMaxAge方法设置了cookie的有效期，比如设置了30分钟，那么当服务器把cookie发送给浏览器时，此时cookie就会在客户端的硬盘上存储30分钟，在30分钟内，即使浏览器关了，cookie依然存在，在30分钟内，打开浏览器访问服务器时，浏览器都会把cookie一起带上，这样就可以在服务器端获取到客户端浏览器传递过来的cookie里面的信息了，这就是cookie设置maxAge和不设置maxAge的区别，不设置maxAge，那么cookie就只在一次会话中有效，一旦用户关闭了浏览器，那么cookie就没有了，那么浏览器是怎么做到这一点的呢，我们启动一个浏览器，就相当于启动一个应用程序，而服务器回送的cookie首先是存在浏览器的缓存中的，当浏览器关闭时，浏览器的缓存自然就没有了，所以存储在缓存中的cookie自然就被清掉了，而如果设置了cookie的有效期，那么浏览器在关闭时，就会把缓存中的cookie写到硬盘上存储起来，这样cookie就能够一直存在。

6. `public int getMaxAge()`

   > 获取Cookies的有效期

7. `public void setPath(String Url)`

   > 设置cookie的有效路径，比如把cookie的有效路径设置为"/xdp"，那么浏览器访问"xdp"目录下的web资源时，都会带上cookie，再比如把cookie的有效路径设置为"/xdp/gacl"，那么浏览器只有在访问"xdp"目录下的"gacl"这个目录里面的web资源时才会带上cookie一起访问，而当访问"xdp"目录下的web资源时，浏览器是不带cookie的

8. `public String getPath()`

   > 获取cookie的有效路径

9. `public void setDomain(String pattern)`

   > 设置cookie的有效域

10. `public String getDomain()`

    > 获取cookie的有效域

###### response 接口也中定义了一个 addCookie 方法，它用于在其响应头中增加一个相应的 Set-Cookie 头字段。 同样，request 接口中也定义了一个 getCookies 方法，它用于获取客户端提交的 Cookie。

```java
package top.zhoudl.cookie;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie实例：获取用户上一次访问的时间
 */
public class CookieDemo01 extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //设置服务器端以UTF-8编码进行输出
        response.setCharacterEncoding("UTF-8");
        //设置浏览器以UTF-8编码进行接收,解决中文乱码问题
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        //获取浏览器访问访问服务器时传递过来的cookie数组
        Cookie[] cookies = request.getCookies();
        //如果用户是第一次访问，那么得到的cookies将是null
        if (cookies!=null) {
            out.write("您上次访问的时间是：");
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals("lastAccessTime")) {
                    Long lastAccessTime =Long.parseLong(cookie.getValue());
                    Date date = new Date(lastAccessTime);
                    out.write(date.toLocaleString());
                }
            }
        }else {
            out.write("这是您第一次访问本站！");
        }
        
        //用户访问过之后重新设置用户的访问时间，存储到cookie中，然后发送到客户端浏览器
        Cookie cookie = new Cookie("lastAccessTime", System.currentTimeMillis()+"");//创建一个cookie，cookie的名字是lastAccessTime
        //将cookie对象添加到response对象中，这样服务器在输出response对象中的内容时就会把cookie也输出到客户端浏览器
        response.addCookie(cookie);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
```

### session

1. session 的处理:
2. 浏览器第一次访问服务器，服务器会创建一个 session，并生成一个 sessionId
3. 将 sessionid 及对应的 session 分别作为 key 和 value 保存到缓存中，也可以持久化到数据库中
4. 服务器再把 sessionid，以 cookie 的形式发送给客户端
5. 浏览器下次再访问时，会直接带着 cookie 中的 sessionid。然后服务器根据 sessionid 找到对应的 session 进行匹配；
6. session 常用方法

#### 基于session的用户认证

![](https:////upload-images.jianshu.io/upload_images/7066270-701c2e1b9f01b9de.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/569/format/webp)

#### session 常用 API

`public void setAttribute(String name,String value)`

> 设定指定名字的属性的值，并将它添加到session会话范围内，如果这个属性是会话范围内存在，则更改该属性的值。

` public Object getAttribute(String name)`

> 在会话范围内获取指定名字的属性的值，返回值类型为object，如果该属性不存在，则返回null。

`public void removeAttribute(String name)`

> 删除指定名字的session属性，若该属性不存在，则出现异常。  

`public void invalidate（）`

> 使session失效。可以立即使当前会话失效，原来会话中存储的所有对象都不能再被访问。  

`public String getId( )`

>  获取当前的会话ID。每个会话在服务器端都存在一个唯一的标示sessionID，session对象发送到浏览器的唯一数据就是sessionID，它一般存储在cookie中。  

`public void setMaxInactiveInterval(int interval)`

> 设置会话的最大持续时间，单位是秒，负数表明会话永不失效。  

`public int getMaxInActiveInterval（）`

> 获取会话的最大持续时间。  

使用 session 对象的` getCreationTime()` 和` getLastAccessedTime()` 方法可以获取会话创建的时间和最后访问的时间，但其返回值是毫秒。

### token(访问资源的令牌)

#### token处理流程:

1. 把用户的用户名和密码发到后端
2. 后端进行校验，校验成功会生成token, 把token发送给客户端
3. 客户端自己保存token, 再次请求就要在Http协议的请求头中带着token去访问服务端，和在服务端保存的token信息进行比对校验。

###### 基于token的认证方案

![](https:////upload-images.jianshu.io/upload_images/7066270-11a9bd97cf03d6f3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/687/format/webp)

#### JWT(json web token)

JWT大致流程如下图所示：

   ![](https:////upload-images.jianshu.io/upload_images/7066270-05351fa25fdfd0ec.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/762/format/webp)

##### 组成：

 一个jwt实际上就是一个字符串，它由三部分组成，头部、载荷与签名，这三个部分都是json格式。

 **一、头部（Header）**

 头部用于描述关于该JWT的最基本的信息，例如其类型以及签名所用的算法等。

```
{
  "typ": "JWT",
  "alg": "HS256"
}
```

在这里，我们说明了这是一个JWT，并且我们所用的签名算法是HS256算法。
 **二、载荷（Payload）**

```
{
    "iss": "John Wu JWT",
    "iat": 1441593502,
    "exp": 1441594722,
    "aud": "www.example.com",
    "sub": "jrocket@example.com",
    "from_user": "B",
    "target_user": "A"
}
```

验证流程：

1. 在头部信息中声明加密算法和常量， 然后把header使用json转化为字符串
2. 在载荷中声明用户信息，同时还有一些其他的内容；再次使用json 把载荷部分进行转化，转化为字符串
3. 使用在header中声明的加密算法和每个项目随机生成的secret来进行加密， 把第一步分字符串和第二部分的字符串进行加密， 生成新的字符串。词字符串是独一无二的。
4. 解密的时候，只要客户端带着JWT来发起请求，服务端就直接使用secret进行解密。
    这里面的前五个字段都是由JWT的标准所定义的。

- iss: 该JWT的签发者
- sub: 该JWT所面向的用户
- aud: 接收该JWT的一方
- exp(expires): 什么时候过期，这里是一个Unix时间戳
- iat(issued at): 在什么时候签发的
   把头部和载荷分别进行Base64编码之后得到两个字符串，然后再将这两个编码后的字符串用英文句号.连接在一起（头部在前），形成新的字符串：

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJmcm9tX3VzZXIiOiJCIiwidGFyZ2V0X3VzZXIiOiJBIn0
```

**三、签名(signature)**
 最后，我们将上面拼接完的字符串用HS256算法进行加密。在加密的时候，我们还需要提供一个密钥（secret）。加密后的内容也是一个字符串，最后这个字符串就是签名，把这个签名拼接在刚才的字符串后面就能得到完整的jwt。header部分和payload部分如果被篡改，由于篡改者不知道密钥是什么，也无法生成新的signature部分，服务端也就无法通过，在jwt中，消息体是透明的，使用签名可以保证消息不被篡改。
 **特点**：

1. 三部分组成，每一部分都进行字符串的转化
2. 解密的时候没有使用数据库，仅仅使用的是secret进行解密。

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJmcm9tX3VzZXIiOiJCIiwidGFyZ2V0X3VzZXIiOiJBIn0.rSWamyAYwuHCo7IFAgd1oRpSP7nzL7BF5t7ItqpKViM
```

###### 和session的区别:

基于session和基于jwt的方式的主要区别就是用户的状态保存的位置，session是保存在服务端的，而jwt是保存在客户端的。

- 应用程序分布式部署的情况下，session需要做多机数据共享，通常可以存在数据库或者redis里面。而jwt不需要。
- jwt不在服务端存储任何状态。RESTful API的原则之一是无状态，发出请求时，总会返回带有参数的响应，不会产生附加影响。用户的认证状态引入这种附加影响，这破坏了这一原则。另外jwt的载荷中可以存储一些常用信息，用于交换信息，有效地使用 JWT，可以降低服务器查询数据库的次数。

###### jwt的缺点：

1. 安全性
    由于jwt的payload是使用base64编码的，并没有加密，因此jwt中不能存储敏感数据。而session的信息是存在服务端的，相对来说更安全。
2. 性能
    jwt太长。由于是无状态使用JWT，所有的数据都被放到JWT里，如果还要进行一些数据交换，那载荷会更大，经过编码之后导致jwt非常长，cookie的限制大小一般是4k，cookie很可能放不下，所以jwt一般放在local storage里面。并且用户在系统中的每一次http请求都会把jwt携带在Header里面，http请求的Header可能比Body还要大。而sessionId只是很短的一个字符串，因此使用jwt的http请求比使用session的开销大得多。
3. 一次性
    无状态是jwt的特点，但也导致了这个问题，jwt是一次性的。想修改里面的内容，就必须签发一个新的jwt。
    （1）无法废弃
    通过上面jwt的验证机制可以看出来，一旦签发一个jwt，在到期之前就会始终有效，无法中途废弃。例如你在payload中存储了一些信息，当信息需要更新时，则重新签发一个jwt，但是由于旧的jwt还没过期，拿着这个旧的jwt依旧可以登录，那登录后服务端从jwt中拿到的信息就是过时的。为了解决这个问题，我们就需要在服务端部署额外的逻辑，例如设置一个黑名单，一旦签发了新的jwt，那么旧的就加入黑名单（比如存到redis里面），避免被再次使用。
    （2）续签
    如果你使用jwt做会话管理，传统的cookie续签方案一般都是框架自带的，session有效期30分钟，30分钟内如果有访问，有效期被刷新至30分钟。一样的道理，要改变jwt的有效时间，就要签发新的jwt。最简单的一种方式是每次请求刷新jwt，即每个http请求都返回一个新的jwt。这个方法不仅暴力不优雅，而且每次请求都要做jwt的加密解密，会带来性能问题。另一种方法是在redis中单独为每个jwt设置过期时间，每次访问时刷新jwt的过期时间。
    可以看出想要破解jwt一次性的特性，就需要在服务端存储jwt的状态。但是引入 redis 之后，就把无状态的jwt硬生生变成了有状态了，违背了jwt的初衷。而且这个方案和session都差不多了。

  

作者：Tiger_Lam

链接：https://www.jianshu.com/p/a99245143a61
