package top.zhoudl.designpatterns.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 16:15
 * @description: 懒汉式单例  线程不安全
 */
@Slf4j
public class LazySingleton01 {

    private LazySingleton01(){}

    public static LazySingleton01 lazySingleton = null;

    public static LazySingleton01 getInstance() {
        if(null == lazySingleton) {
            return new LazySingleton01();
        }
        return lazySingleton;
    }

}
