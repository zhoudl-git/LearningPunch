package top.zhoudl.designpatterns.singleton;

import java.io.Serializable;

/**
 * @author: zhoudongliang
 * @date: 2019/5/10 17:24
 * @description:
 */
public class SeriableSingleton implements Serializable {

    public final static SeriableSingleton INSTANCE = new SeriableSingleton();

    private SeriableSingleton(){}

    public static SeriableSingleton getInstance(){
        return INSTANCE;
    }

    private Object readResolve(){
        return INSTANCE;
    }
}
