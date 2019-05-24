package top.zhoudl.designpatterns.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 16:15
 * @description: 懒汉式单例
 */
@Slf4j
public class LazySingleton02 {

    private LazySingleton02(){}

    private static volatile LazySingleton02 lazySingleton = null;

    public static synchronized LazySingleton02 getInstance() {
        if(null == lazySingleton) {
            lazySingleton = new LazySingleton02();
        }
        return lazySingleton;
    }

}
