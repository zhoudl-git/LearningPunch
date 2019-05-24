package top.zhoudl.designpatterns.factory.factorymethod;

import top.zhoudl.designpatterns.factory.simplefactory.ICourse;
import top.zhoudl.designpatterns.factory.simplefactory.JavaCourse;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:13
 * @description:
 */
public class JavaCourseFactory implements ICourseFactory{
    @Override
    public ICourse createCourse() {
        return new JavaCourse();
    }
}
