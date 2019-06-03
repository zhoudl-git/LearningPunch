package top.zhoudl.nettystudy.protocol;

import io.netty.util.AttributeKey;

/**
 * @author: zhoudongliang
 * @date: 2019/6/3 09:33
 * @description:
 */
public interface Attributes {

    /**
     * 登录成功标记属性
     */
    AttributeKey<Boolean> LOGIN = AttributeKey.newInstance("login");

}
