package top.zhoudl.nettystudy.protocol.serialize;

import com.alibaba.fastjson.serializer.JSONSerializer;
import top.zhoudl.nettystudy.protocol.serialize.impl.JSONSerializationImpl;

/**
 * @author: zhoudongliang
 * @date: 2019/5/27 11:20
 * @description: 序列化接口 Serialization
 */
public interface Serializer {

    /**
     * 默认序列化算法
     */
    Serializer DEFAULT = new JSONSerializationImpl();

    /**
     * 序列化算法
     */
    byte getSerializationAlgorithm();

    /**
     * java 对象转换成二进制数据
     */
    byte[] serialization(Object object);

    /**
     * 二进制转换成 java 对象
     */
    <T> T deserialization(Class<T> clazz, byte[] bytes);

}
