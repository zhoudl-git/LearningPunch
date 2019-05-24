package top.zhoudl.designpatterns.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 16:15
 * @description: 懒汉式单例 - 双重检查锁
 */
@Slf4j
public class LazySingleton03 {

    private LazySingleton03(){}

    private static volatile LazySingleton03 lazySingleton = null;

    public static synchronized LazySingleton03 getInstance() {
        if(null == lazySingleton) {
            synchronized (LazySingleton03.class) {
                if(null == lazySingleton) {
                    lazySingleton = new LazySingleton03();
                }
            }
        }
        return lazySingleton;
    }

}
