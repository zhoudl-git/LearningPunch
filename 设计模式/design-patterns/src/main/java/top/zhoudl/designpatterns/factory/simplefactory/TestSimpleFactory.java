package top.zhoudl.designpatterns.factory.simplefactory;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 14:31
 * @description: 简单工厂模式
 */

public class TestSimpleFactory {

    public static void main(String[] args) {
        // 方式一
        /*ICourse javaCourse = new JavaCourse();
        ICourse pythonCourse = new PythonCourse();

        javaCourse.study();
        pythonCourse.study();*/

        // 方式二
        /*CourseFactory.createCourse_1("java").study();
        CourseFactory.createCourse_1("python").study();*/

        // 方式三 使用反射
        /*try {
            CourseFactory.createCourse_2("top.zhoudl.designpatterns.factory.simplefactory.JavaCourse").study();
            CourseFactory.createCourse_2("top.zhoudl.designpatterns.factory.simplefactory.PythonCourse").study();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }*/

        // 方式四 限定传递参数
        try {
            CourseFactory.createCourse_3(JavaCourse.class).study();
            CourseFactory.createCourse_3(PythonCourse.class).study();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        /**
         * 实际应用中
         * JDK 中的 Calandar 类 创建对象的时候使用的便是简单工厂模式
         * 简单工厂模式有个弊端就是：工厂类的职责相对过重，不易于扩展过于复杂的产品结构
         */

    }
}