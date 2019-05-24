package top.zhoudl.designpatterns.factory.factorymethod;


/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:15
 * @description: 工厂方法模式
 */
public class TestFactoryMethod {

    public static void main(String[] args) {

        ICourseFactory courseFactory = null;

        courseFactory = new JavaCourseFactory();
        courseFactory.createCourse().study();

        courseFactory = new PythonCourseFactory();
        courseFactory.createCourse().study();

        /**
         * Logback 中大量运用了 工厂方法模式来创建对象
         *
         * 小结：
         * 1、创建对象需要大量重复的代码。
         * 2、客户端（应用层）不依赖于产品类实例如何被创建、实现等细节。
         * 3、一个类通过其子类来指定创建哪个对象。
         *
         * 工厂方法也有缺点：
         * 1、类的个数容易过多，增加复杂度。
         * 2、增加了系统的抽象性和理解难度。
         */
    }

}
