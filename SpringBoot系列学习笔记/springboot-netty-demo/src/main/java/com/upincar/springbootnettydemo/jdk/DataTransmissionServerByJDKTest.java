package com.upincar.springbootnettydemo.jdk;

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
