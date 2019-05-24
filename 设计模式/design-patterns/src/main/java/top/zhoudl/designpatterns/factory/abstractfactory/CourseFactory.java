package top.zhoudl.designpatterns.factory.abstractfactory;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:37
 * @description: 抽象工厂
 */
public interface CourseFactory {

    /**
     * 创建视频
     * @return
     */
    IVideo createVideo();

    /**
     * 创建笔记
     * @return
     */
    INote createNote();

}
