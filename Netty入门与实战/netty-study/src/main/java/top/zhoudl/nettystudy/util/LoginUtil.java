package top.zhoudl.nettystudy.util;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import top.zhoudl.nettystudy.protocol.Attributes;

/**
 * @author: zhoudongliang
 * @date: 2019/6/3 09:43
 * @description:
 */
public class LoginUtil {

    public static void markAsLogin(Channel channel) {
        channel.attr(Attributes.LOGIN).set(true);
    }

    /**
     * 如果有标志位，不管标志位的值是什么，都表示已经成功登录过
     * @param channel
     * @return
     */
    public static boolean hasLogin(Channel channel) {
        Attribute<Boolean> loginAttr = channel.attr(Attributes.LOGIN);
        return loginAttr.get() != null;
    }
}
