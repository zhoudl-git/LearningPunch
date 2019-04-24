package com.upincar.springbootnettydemo.jdk;

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
