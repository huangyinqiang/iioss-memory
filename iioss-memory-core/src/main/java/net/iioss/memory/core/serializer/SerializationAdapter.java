package net.iioss.memory.core.serializer;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.definition.SerializerType;
import net.iioss.memory.core.exception.DeSerializerException;
import net.iioss.memory.core.exception.SerializerException;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.serializer
 * @Description: 序列化适配器
 * @date 2019/6/9 14:03
 */
@Slf4j
public class SerializationAdapter {

    private static Serializer serializer;

    /**
     * 初始化序列化器
     * @param serializerProgramme 指定的序列化器名，或者类名
     */
    public static void init(String serializerProgramme) {
        SerializerType serializerType = SerializerType.
                getSerializerTypeByName(
                        StrUtil.emptyToDefault(serializerProgramme, SerializerType.FST.getName()));
        if (ObjectUtil.isNull(serializerType)){
            throw new SerializerException(StrUtil.format("不支持此序列化方案 -> [{}]", serializerProgramme));
        }

        Class<?> serializerClass=null;
        try {
            serializerClass= ObjectUtil.isNotEmpty(serializerType)?
                    ClassUtil.loadClass(serializerType.getClassName()):ClassUtil.loadClass(serializerType.getClassName());
            serializer = (Serializer) serializerClass.newInstance();
        } catch (Exception e) {
            throw new SerializerException(StrUtil.format("加载序列化类失败 -> [方案名：{}，class: {}]", serializerProgramme,serializerClass),e);
        }
        log.info("选用序列化方案 -> [{}:{}]", serializer.name(), ClassUtil.getClassName(serializer,false));
    }

    /**
     * 序列化
     * @param obj　序列化对象
     * @return 字节流数组
     * @throws SerializerException
     */
    public static byte[] serialize(Object obj) throws SerializerException {
        return ObjectUtil.isNull(obj)?null:serializer.serialize(obj);
    }

    /**
     * 反序列化
     * @param bytes 待反序列化的字节数组
     * @return 序列化后的对象
     * @throws DeSerializerException
     */
    public static Object deserialize(byte[] bytes) throws DeSerializerException {
        return ObjectUtil.isEmpty(bytes)?null:serializer.deserialize(bytes);
    }

}
