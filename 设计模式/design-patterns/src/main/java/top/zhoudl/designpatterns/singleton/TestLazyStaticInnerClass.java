package top.zhoudl.designpatterns.singleton;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 17:45
 * @description:
 */
public class TestLazyStaticInnerClass {

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Class<LazyStaticInnerClass> clazz = LazyStaticInnerClass.class;
        // 获取构造方法
        Constructor constructors = clazz.getDeclaredConstructor(null);
        // 允许访问私有属性
        constructors.setAccessible(true);

        Object object01 = constructors.newInstance();
        Object object02 = constructors.newInstance();

        System.out.println(object01 == object02);


    }
}
