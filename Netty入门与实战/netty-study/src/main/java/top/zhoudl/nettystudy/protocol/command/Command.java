package top.zhoudl.nettystudy.protocol.command;

/**
 * @author: zhoudongliang
 * @date: 2019/5/27 11:15
 * @description: 指令
 */
public interface Command {

    /**
     * 登录指令
     */
    Byte LOGIN_REQUEST = 1;

    /**
     * 登录响应指令
     */
    Byte LOGIN_RESPONSE = 2;

}
