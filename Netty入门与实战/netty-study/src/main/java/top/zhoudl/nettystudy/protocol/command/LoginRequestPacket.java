package top.zhoudl.nettystudy.protocol.command;

import lombok.Data;

/**
 * @author: zhoudongliang
 * @date: 2019/5/27 11:16
 * @description: 登录请求数据包
 *
 * 继承了 Packet 并实现了 getCommand() 方法，返回了一个常量，表示为登录请求
 */
@Data
public class LoginRequestPacket extends Packet{

    /**
     * 用户 ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    @Override
    public Byte getCommand() {
        return Command.LOGIN_REQUEST;
    }
}
