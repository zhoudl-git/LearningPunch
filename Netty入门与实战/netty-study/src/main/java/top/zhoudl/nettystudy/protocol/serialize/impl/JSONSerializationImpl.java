package top.zhoudl.nettystudy.protocol.serialize.impl;

import com.alibaba.fastjson.JSON;
import top.zhoudl.nettystudy.protocol.serialize.SerializationAlgorithm;
import top.zhoudl.nettystudy.protocol.serialize.Serializer;

/**
 * @author: zhoudongliang
 * @date: 2019/5/27 11:22
 * @description: 序列化算法 此处使用 fastjson
 */
public class JSONSerializationImpl implements Serializer {
    @Override
    public byte getSerializationAlgorithm() {
        return SerializationAlgorithm.FASTJSON;
    }

    @Override
    public byte[] serialization(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialization(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
