package top.zhoudl.nettystudy.protocol.command;

import lombok.Data;

/**
 * @author: zhoudongliang
 * @date: 2019/5/29 16:10
 * @description:
 */
@Data
public class LoginResponsePacket extends Packet {
    /**
     * 响应是否成功
     */
    private boolean success;

    /**
     * 响应失败原因
     */
    private String reason;

    @Override
    public Byte getCommand() {
        return Command.LOGIN_RESPONSE;
    }
}
