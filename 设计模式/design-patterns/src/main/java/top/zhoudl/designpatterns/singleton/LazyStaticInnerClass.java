package top.zhoudl.designpatterns.singleton;


/**
 * @author: zhoudongliang
 * @date: 2019/5/5 17:31
 * @description: 使用静态内部类的方式实现
 */
public class LazyStaticInnerClass {

    private LazyStaticInnerClass(){
        // 避免了反射破坏单例的可能性 当检测到重复创建对象的时候直接抛出异常
        if(null != LazyInner.LAZY) {
            throw new RuntimeException("---单例对象不允许创建多个实例---");
        }
    }

    /**
     * final 关键字 保证该方法不会被重写 重载
     * static 关键字  为了使得单例的空间共享
     * @return
     */
    public static final LazyStaticInnerClass getInstance() {
        return LazyInner.LAZY;
    }

    /**
     * 静态内部类
     * 在外部类没有被调用的时候 是不会主动执行的 这种形式兼顾这种方式唯一存在的问题就是会被 反序列化 破坏单例。饿汉式的内存浪费，也兼顾 synchronized 加锁的性能问题
     *
     * 序列化
     * 把内存中的状态转化为字节码状态，从而转换成一个 IO 流，
     * 然后存储到其他设备（磁盘，其他网络中等等），从而把内存状态永久的保存下来。
     *
     * 反序列化
     * 把已经序列化的字节码通过 IO 流读取，读取过程中转化成对应的 Java 对象，这个过程中会 new 对象，从而破坏掉单例。
     */
    private static class LazyInner {
        private static final LazyStaticInnerClass LAZY = new LazyStaticInnerClass();
    }


}
