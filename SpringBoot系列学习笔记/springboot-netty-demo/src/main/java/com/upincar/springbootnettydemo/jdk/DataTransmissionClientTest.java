package com.upincar.springbootnettydemo.jdk;

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
