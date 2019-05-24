package top.zhoudl.designpatterns.proxy.staticproxy;

import top.zhoudl.designpatterns.proxy.Person;

/**
 * @author: zhoudongliang
 * @date: 2019/5/13 14:44
 * @description: 父亲实体
 */
public class Father implements Person {

    /**
     * 要帮儿子找对象
     */
    private Son son;

    public Father(Son son) {
        this.son = son;
    }

    @Override
    public void findLove() {
        System.out.println("父母物色对象");
        this.son.findLove();
        System.out.println("双方同意交往，确立关系");
    }
}
