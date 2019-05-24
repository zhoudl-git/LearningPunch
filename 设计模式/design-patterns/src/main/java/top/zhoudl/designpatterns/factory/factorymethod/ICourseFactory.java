package top.zhoudl.designpatterns.factory.factorymethod;

import top.zhoudl.designpatterns.factory.simplefactory.ICourse;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:12
 * @description:
 */
public interface ICourseFactory {

    ICourse createCourse();

}
