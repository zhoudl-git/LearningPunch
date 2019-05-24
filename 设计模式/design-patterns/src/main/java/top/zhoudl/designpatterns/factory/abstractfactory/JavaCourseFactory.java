package top.zhoudl.designpatterns.factory.abstractfactory;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:41
 * @description: 创建 Java 产品族的工厂
 */
public class JavaCourseFactory implements CourseFactory {
    @Override
    public IVideo createVideo() {
        return new JavaVideo();
    }

    @Override
    public INote createNote() {
        return new JavaNote();
    }
}
