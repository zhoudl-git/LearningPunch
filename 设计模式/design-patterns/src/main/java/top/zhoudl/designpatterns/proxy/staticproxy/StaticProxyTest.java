package top.zhoudl.designpatterns.proxy.staticproxy;

/**
 * @author: zhoudongliang
 * @date: 2019/5/13 14:50
 * @description:
 */
public class StaticProxyTest {


    /**
     * 静态代理方式具有局限性，只能帮儿子找对象
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        Father father = new Father(new Son());
        father.findLove();
    }

}
