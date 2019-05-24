package top.zhoudl.designpatterns.proxy.staticproxy;

import top.zhoudl.designpatterns.proxy.Person;

/**
 * @author: zhoudongliang
 * @date: 2019/5/13 14:42
 * @description: 儿子实体
 */
public class Son implements Person {

    @Override
    public void findLove() {
        System.out.println("肤白貌美大长腿！");
    }
}
