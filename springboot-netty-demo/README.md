### Netty相关API介绍（一）

#### 使用 JDK API 实现一个简单的客户端服务端数据传输案例

##### OIO （阻塞方式）

- 服务端代码

  ```java
  import java.io.*;
  import java.net.ServerSocket;
  import java.net.Socket;
  import java.nio.charset.Charset;
  
  /**
   * @author: zhoudongliang
   * @date: 2018/9/18 16:30
   * @description: 使用JDK API 实现客户端服务端通信 服务端
   */
  public class DataTransmissionServerByJDKTest {
  
      public static void server(int port) throws IOException {
          final ServerSocket socket = new ServerSocket(port);
          try {
              for (; ; ) {
                  final Socket clientSocket = socket.accept();
                  System.out.println(
                          " 接收客户端请求 " + clientSocket);
                  new Thread(() -> {
                      OutputStream out = null;
                      InputStream is = null;
                      BufferedReader br = null;
                      try {
                          // 写消息给客户端
                          out = clientSocket.getOutputStream();
                          out.write("Hello 我是服务端".getBytes(
                                  Charset.forName("UTF-8")));
                          out.flush();
  
                          // 接收客户端消息
                          is = clientSocket.getInputStream();
                          br = new BufferedReader(new InputStreamReader(is));
                          String info;
                          while ((info = br.readLine()) != null) {
                              System.out.println("我是服务端，客户端说：" + info);
                          }
                          clientSocket.close();
                      } catch (IOException e) {
                          e.printStackTrace();
                      } finally {
                          try {
                              out.close();
                              is.close();
                              br.close();
                              clientSocket.close();
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }
                  }).start();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  
      public static void main(String[] args) {
          try {
              server(10086);
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  }
  ```

- 客户端代码

  ```java
  import java.io.*;
  import java.net.Socket;
  
  /**
   * @author: zhoudongliang
   * @date: 2018/9/18 16:39
   * @description: 使用JDK API 实现客户端服务端通信 客户端
   */
  public class DataTransmissionClientTest {
  
      public static void client(int port){
          //客户端
          Socket clientSocket = null;
          InputStream is = null;
          BufferedReader br = null;
          OutputStream os = null;
          PrintWriter pw = null;
          try {
              //1、创建客户端Socket，指定服务器地址和端口
              clientSocket = new Socket("localhost", port);
              //2、获取输出流，向服务器端发送信息
              // 字节输出流
              os = clientSocket.getOutputStream();
              // 将输出流包装成打印流
              pw = new PrintWriter(os);
              pw.write("用户名：admin；密码：123");
              pw.flush();
              clientSocket.shutdownOutput();
              //3、获取输入流，并读取服务器端的响应信息
              is = clientSocket.getInputStream();
              br = new BufferedReader(new InputStreamReader(is));
              String info;
              while ((info = br.readLine()) != null) {
                  System.out.println("我是客户端，服务端说：" + info);
              }
          } catch (IOException e) {
              e.printStackTrace();
          } finally {
              //4、关闭资源
              try {
                  br.close();
                  is.close();
                  pw.close();
                  os.close();
                  clientSocket.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
  
      public static void main(String[] args) {
          client(10086);
      }
  }
  ```

- 输出结果

  服务端

  > 接收客户端请求 Socket[addr=/127.0.0.1,port=58584,localport=10086]
  > 我是服务端，客户端说：用户名：admin；密码：123

  客户端

  > 我是客户端，服务端说：Hello 我是服务端

  **具体端口和IP地址式自己情况而定**  

- 缺点 ：使用了OIO 会造成阻塞 从而降低了资源利用率

- ##### NIO （非阻塞方式，非Netty方式）


```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @author: zhoudongliang
 * @date: 2018/9/18 16:59
 * @description: 使用NIO方式实现服务端
 */
public class DataTransmissionServerNioByJDKTest {

    public static void server(int port) throws IOException {
        //定义处理编码和解码的字符集
        Charset charset = Charset.forName("UTF-8");
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket socket = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        socket.bind(address);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        final ByteBuffer msg = ByteBuffer.wrap("Hello 我是服务端\r\n".getBytes());
        for (; ; ) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server =
                                (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE |
                                SelectionKey.OP_READ, msg.duplicate());
                        System.out.println(
                                " 接收客户端请求 " + client);
                    }
                    if(key.isReadable()) {
                        //使用NIO读取channel中的数据
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buff = ByteBuffer.allocate(1024);
                        // 读取客户端数据
                        String content = "";
                        while (sc.read(buff) > 0) {
                            buff.flip();
                            content += charset.decode(buff);
                        }
                        //打印输出读取的内容
                        System.out.println(" 客户端说 " + content);
                        //为下一次读取做准备
                        key.interestOps(SelectionKey.OP_READ);

                    }
                    if (key.isWritable()) {
                        // 向客户端写数据
                        SocketChannel client =
                                (SocketChannel)key.channel();
                        ByteBuffer buffer =
                                (ByteBuffer)key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        client.close();
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        cex.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            server(10086);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```





