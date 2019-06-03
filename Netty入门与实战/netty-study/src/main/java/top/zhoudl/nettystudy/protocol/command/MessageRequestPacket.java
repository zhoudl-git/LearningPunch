package top.zhoudl.nettystudy.protocol.command;

import lombok.Data;

/**
 * @author: zhoudongliang
 * @date: 2019/5/30 10:23
 * @description: 请求消息对象
 */
@Data
public class MessageRequestPacket extends Packet{

    private String message;

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_REQUEST;
    }

}
