package top.zhoudl.designpatterns.factory.abstractfactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:40
 * @description: 产品族
 */
@Slf4j
public class JavaVideo implements IVideo{

    @Override
    public void video() {
        log.info(" Java 视频 。。。");
    }
}
