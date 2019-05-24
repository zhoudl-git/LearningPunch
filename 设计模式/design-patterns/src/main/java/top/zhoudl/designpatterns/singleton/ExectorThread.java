package top.zhoudl.designpatterns.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 16:19
 * @description:
 */
@Slf4j
public class ExectorThread implements Runnable{

    @Override
    public void run() {
        // 懒汉式 线程不安全
        //LazySingleton01 singleton = LazySingleton01.getInstance();
        // 懒汉式 线程安全 直接在 instance 方法加锁
        LazySingleton02 singleton = LazySingleton02.getInstance();
        // 懒汉式 线程安全 双重检查锁提高效率
        //LazySingleton03 singleton = LazySingleton03.getInstance();
        // 饿汉式 线程安全
        //HungrySigleton singleton = HungrySigleton.getIntance();
        log.info(Thread.currentThread().getName() + ":" + singleton);
    }
}
