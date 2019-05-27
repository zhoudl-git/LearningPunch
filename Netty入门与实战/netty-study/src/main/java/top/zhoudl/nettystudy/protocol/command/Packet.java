package top.zhoudl.nettystudy.protocol.command;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author: zhoudongliang
 * @date: 2019/5/27 11:13
 * @description: 协议包
 */
@Data
public abstract class Packet {

    /**
     * 协议版本号
     */
    @JSONField(deserialize = false, serialize = false)
    private Byte version = 1;

    /**
     * 指令
     * @return
     */
    @JSONField(serialize = false)
    public abstract Byte getCommand();

}
