package top.zhoudl.designpatterns.factory.simplefactory;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 14:35
 * @description:
 */
public class CourseFactory {

    public static ICourse createCourse_1(String course) {

        // 根据参数判断，但是这样每次增加分支都需要修改代码
        if("java".equals(course)) {
            return new JavaCourse();
        } else if ("python".equals(course)) {
            return new PythonCourse();
        } else {
            return null;
        }
    }

    public static ICourse createCourse_2(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // 直接使用反射创建对象
        if(!("".equals(className) && null != className)) {
            return (ICourse) Class.forName(className).newInstance();
        } else {
            return null;
        }
    }

    public static ICourse createCourse_3(Class<? extends ICourse> clazz) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(null != clazz) {
            return clazz.newInstance();
        } else {
            return null;
        }
    }
}
