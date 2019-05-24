package top.zhoudl.designpatterns.factory.simplefactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 14:13
 * @description:
 */
@Slf4j
public class JavaCourse implements ICourse {

    @Override
    public void study() {
        log.info(" Java Study ...");
    }
}
