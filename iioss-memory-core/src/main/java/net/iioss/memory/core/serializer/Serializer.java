package net.iioss.memory.core.serializer;

import cn.hutool.core.util.ClassUtil;
import net.iioss.memory.core.constant.SerializerType;
import net.iioss.memory.core.exception.DeSerializerException;
import net.iioss.memory.core.exception.SerializerException;

import java.util.Objects;

/**
 * @Title 序列化接口
 * @auther huangyinqiang
 * @create 2019-06-05 下午4:54
 */
public interface Serializer {

    /**
     * 获取序列化方案的名称
     * @return
     */
    default String name() {


        return Objects.requireNonNull(SerializerType.
                getSerializerTypeByClassName(
                        ClassUtil.getClassName(this, false)))
                .getName();
    }

    /**
     * 将对象进行序列化操作
     * @param obj　对象
     * @return 　  字节流数组
     * @throws SerializerException　
     */
    byte[] serialize(Object obj) throws SerializerException;

    /**
     * 反序列化
     * @param bytes　字节流数组
     * @return       转换后的对象
     * @throws DeSerializerException
     */
    Object deserialize(byte[] bytes) throws DeSerializerException;
}
