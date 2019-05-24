package top.zhoudl.designpatterns.factory.abstractfactory;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:43
 * @description: 抽象工厂模式测试
 */
public class TestAbstractFactory {

    public static void main(String[] args) {
        new JavaCourseFactory().createNote().wirteNote();
        new JavaCourseFactory().createVideo().video();
    }

    /**
     * 上面的代码完整地描述了两个产品族 Java 课程和 Python 课程，也描述了两个产品等级
     * 视频和手记。抽象工厂非常完美清晰地描述这样一层复杂的关系。但是，不知道大家有
     * 没有发现，如果我们再继续扩展产品等级，将源码 Source 也加入到课程中，那么我们的
     * 代码从抽象工厂，到具体工厂要全部调整，很显然不符合开闭原则。因此抽象工厂也是
     * 有缺点的：
     * 1、规定了所有可能被创建的产品集合，产品族中扩展新的产品困难，需要修改抽象工厂
     * 的接口。
     * 2、增加了系统的抽象性和理解难度。
     */

}
