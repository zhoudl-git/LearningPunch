package top.zhoudl.designpatterns.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 16:11
 * @description: 饿汉式单例
 */
@Slf4j
public class HungrySigleton {

    /**
     * 方式一 直接创建对象
     */
    private static HungrySigleton hungrySigleton = new HungrySigleton();

    private HungrySigleton() {}

    /**
     * 方式二 使用静态代码块
     */
    static {
        hungrySigleton = new HungrySigleton();
    }

    public static HungrySigleton getIntance() {
        return hungrySigleton;
    }

    public static void main(String[] args) {
        log.info(HungrySigleton.getIntance().toString());
    }

    /**
     * 饿汉式适用在单例对象较少的情况
     */

}
