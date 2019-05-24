package top.zhoudl.designpatterns.factory.abstractfactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhoudongliang
 * @date: 2019/5/5 15:40
 * @description:
 */
@Slf4j
public class JavaNote implements INote{
    @Override
    public void wirteNote() {
        log.info(" Java 笔记 。。。");
    }
}
