> 本系列文章参考内容为 闪电侠 的掘金小册
>
> 地址 ：[Netty入门与实战：仿写微信IM即时通信系统](https://juejin.im/book/5b4bc28bf265da0f60130116/section/5b6a1a9cf265da0f87595521)

## 客户端与服务端通信协议编码

### 什么是服务端和客户端的通信协议？

基于 TCP 通信的数据包格式必须是二进制的。

协议指的就是**客户端与服务端事先商量好的**，每一个二进制数据包中每一段字节分别代表对应含义的**规则**。

比如 这些字节可以代表 指令类型、用户名、密码等等；

#### 客户端与服务端通信过程

* 客户端把一个 Java 对象按照通信协议转换成二进制数据包；
* 通过网络客户端把这个数据包传输到服务端，传送过程有 TCP/IP 协议负责传输；
* 服务端接受到消息之后，根据定义好的协议获取二进制数据包的相应字段信息，然后包装成 Java 对象，交给应用逻辑处理；
* 服务端逻辑处理完成后，如果需要响应客户端，则根据以上流程发送对应响应消息到客户端。

#### 通信协议的设计

![图片来源于闪电侠 掘金小册 - Netty 入门到实战：仿写微信 IM 即时通讯系统](https://user-gold-cdn.xitu.io/2018/8/13/1653028b36ee5d81?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

1. 魔数：在数据包发送消息到服务端之后，服务端会首先取出这四个字节来进行比对，可以最快判断出这个数据包是否是遵循自定义协议的，如果判断出事无效数据包，为了安全起见，会直接关闭连接，这样可以节省资源；
2. 版本号：为了兼容协议升级，属于是预留字段；
3. 序列化算法：表示序列化 Java 对象的时候使用的何种算法；
4. 指令：1 字节 = 8 bit,也就是说一个字节最高可以支持 256 中指令；
5. 数据长度；
6. 数据内容：每一种指令对应的数据是不一样的，如登录时需要用户名和密码，收消息需要用户标识和具体消息内容等等。

#### 通信协议的实现

##### Java 对象的定义

> Packet.java

```java
@Data
public abstract class Packet {

    /**
     * 协议版本号
     */
    @JSONField(deserialize = false, serialize = false)
    private Byte version = 1;

    /**
     * 指令
     * @return
     */
    @JSONField(serialize = false)
    public abstract Byte getCommand();

}
```

此处引入两个其他知识点：

* `@Data` 为 lombock 的注解, 使用 [lombock](https://projectlombok.org/) 可以大幅减少代码量
* `@JSONField` 注解可以用在方法（method），属性（field）以及方法中的参数（parameter）上（详细用法参见 [FastJson 菜鸟简明教程](https://www.runoob.com/w3cnote/fastjson-intro.html)）
  * name 属性用来指定 JSON 串中 key 的名称；
  * 使用 serialize/deserialize 指定字段不序列化；

想详细研究以上两个组件的同学可以自行去搜索，此处不再赘述。

##### 定义指令集接口

>Command.java

```java
public interface Command {

    /**
     * 登录指令
     */
    Byte LOGIN_REQUEST = 1;

}
```

##### 定义登录请求数据包

> LoginRequestPacket.java

```java
@Data
public class LoginRequestPacket extends Packet{

    /**
     * 用户 ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    @Override
    public Byte getCommand() {
        return Command.LOGIN_REQUEST;
    }
}
```

登录请求数据包包含了三个字段：用户ID、用户名和密码。

##### 定义序列化接口

> Serializer.java

```java
public interface Serializer {

    /**
     * 获取具体的序列化算法标识
     */
    byte getSerializationAlgorithm();

    /**
     * java 对象转换成二进制数据
     */
    byte[] serialization(Object object);

    /**
     * 二进制转换成 java 对象
     */
    <T> T deserialization(Class<T> clazz, byte[] bytes);

}
```

##### 定义序列化标识接口

> SerializationAlgorithm.java

```java
public interface SerializationAlgorithm {

    /**
     * json 序列化标识 该值代表使用 fastjson 进行序列化
     */
    byte FASTJSON = 1;

}
```

##### 实现序列化接口

> JSONSerializationImpl.java

```java
public class JSONSerializationImpl implements Serializer {
    @Override
    public byte getSerializationAlgorithm() {
        return SerializationAlgorithm.FASTJSON;
    }

    @Override
    public byte[] serialization(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialization(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
```

##### 给序列化接口增加默认的序列化算法

修改 `Serializer.java` 接口

```java
public interface Serializer {

    /**
     * 默认序列化算法
     */
    Serializer DEFAULT = new JSONSerializationImpl();

    /**
     * 序列化算法
     */
    byte getSerializationAlgorithm();

    /**
     * java 对象转换成二进制数据
     */
    byte[] serialization(Object object);

    /**
     * 二进制转换成 java 对象
     */
    <T> T deserialization(Class<T> clazz, byte[] bytes);

}
```

综上：我们实现了序列化相关的逻辑，如果要使用其他序列化算法的话，只需要实现 Serializer 接口，然后定义一下序列化算法的标识并覆盖 `serialization` 和 `deserialization(Class<T> clazz, byte[] bytes)` 两个方法即可。

接下来开始重头戏，编码 Java 对象为二进制数据

##### 编码

> PacketCodec.java

```java
public class PacketCodec {
    /**
     * 魔数
     */
    private static final int MAGIC_NUMBER = 0x12345678;

    /**
     * 编码数据包
     * @param packet
     * @return
     */
    public ByteBuf encode (Packet packet) {

        // 1. 构建 ByteBuf 对象
        // ioBuffer() 方法会返回适配 io 读写相关的内存，
        // 它会尽可能创建一个直接内存，
        // 直接内存可以理解为不受 jvm 堆管理的内存空间，写到 IO 缓冲区的效果更高。
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
        // 2. 序列化 packet 对象
        byte[] bytes = Serializer.DEFAULT.serialization(packet);
        // 3. 开始编码
        // 魔数
        byteBuf.writeInt(MAGIC_NUMBER);
        // 版本号
        byteBuf.writeByte(packet.getVersion());
        // 序列化算法
        byteBuf.writeByte(Serializer.DEFAULT.getSerializationAlgorithm());
        // 指令
        byteBuf.writeByte(packet.getCommand());
        // 数据长度
        byteBuf.writeInt(bytes.length);
        // 数据
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }
}
```

##### 解码

> PacketCodec.java

```java
public class PacketCodec {
    /**
     * 魔数
     */
    private static final int MAGIC_NUMBER = 0x12345678;

    /**
     * 初始化 序列化算法 和 指令集
     */
    private static final Map<Byte, Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer> serializerMap;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(Command.LOGIN_REQUEST, LoginRequestPacket.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializationImpl();
        serializerMap.put(serializer.getSerializationAlgorithm(), serializer);
    }

    /**
     * 解码数据包
     * @param byteBuf
     * @return
     */
    public Packet decode(ByteBuf byteBuf) {
        // 跳过魔数
        byteBuf.skipBytes(4);
        // 跳过版本号
        byteBuf.skipBytes(1);
        // 序列化算法标识
        byte serializationAlgorithm = byteBuf.readByte();
        // 指令
        byte command = byteBuf.readByte();
        // 数据包长度
        int length = byteBuf.readInt();
        // 数据内容
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializationAlgorithm);

        if (requestType != null && serializer != null) {
            return serializer.deserialization(requestType, bytes);
        }
        return null;
    }

    private Serializer getSerializer(byte serializeAlgorithm) {
        return serializerMap.get(serializeAlgorithm);
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }
}
```

好了，到此解码结束，可以看出来解码和编码就是一个相反的过程，和序列化和反序列化是类似的，应该很好理解。

##### 编写测试类

编码结束，当然要撸一把，才能知道写得对不对。

> PacketCodecTest.java

```java
public class PacketCodecTest {

    @Test
    public void encode() {

        Serializer serializer = new JSONSerializationImpl();
        // 创建登录请求
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();

        loginRequestPacket.setVersion(((byte) 1));
        loginRequestPacket.setUserId(123);
        loginRequestPacket.setUserName("zhoudl");
        loginRequestPacket.setPassword("pwd");

        PacketCodec packetCodec = new PacketCodec();
        // 编码成 ByteBuf
        ByteBuf byteBuf = packetCodec.encode(loginRequestPacket);
        // 解码 ByteBuf 为 Packet
        Packet decodedPacket = packetCodec.decode(byteBuf);

        Assert.assertArrayEquals(serializer.serialization(loginRequestPacket), serializer.serialization(decodedPacket));

    }

}
```

### 总结

* 通信协议是为了客户端和服务端数据交互而双方协商出来的满足一定规则的二进制数据格式（我们自定义的协议本质上和 HTTP 协议是一致的，只不过我们自定义的属于私有协议，HTTP 属于公共协议）；
* Java 对象的序列化，主要目的是为了实现 Java 对象与二进制数据的相互转换；
* 了解编码和解码的过程；