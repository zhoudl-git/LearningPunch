package top.zhoudl.nettystudy.util;

import top.zhoudl.nettystudy.protocol.command.LoginResponsePacket;

import java.util.HashMap;

/**
 * @author: zhoudongliang
 * @date: 2019/6/10 14:30
 * @description:
 */
public class HashMapTest {

    public static void main(String[] args) {
        HashMap<Integer, String> map = new HashMap(16);
        map.put(12, "");
        map.put(23, "");
        map.put(44, "");
        map.put(56, "");
        map.put(2, "");
        map.put(78, "");
        map.put(6, "");
        for(int i=0; i<10;i++) {
            System.out.println("第 " + i + " 次遍历结果：");
            for (Integer key : map.keySet()) {
                System.out.print(key + " -> ");
            }
            System.out.println();
        }
    }


}
