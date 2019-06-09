package net.iioss.memory.core.serializer;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import net.iioss.memory.core.exception.DeSerializerException;
import net.iioss.memory.core.exception.SerializerException;
import org.nustaq.serialization.FSTConfiguration;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.serializer
 * @Description: FST序列化方案
 * @date 2019/6/9 13:51
 */
public class FSTSerializer implements Serializer{
    private FSTConfiguration fstConfiguration ;

    public FSTSerializer() {
        fstConfiguration = FSTConfiguration.getDefaultConfiguration();
        fstConfiguration.setClassLoader(Thread.currentThread().getContextClassLoader());
    }


    @Override
    public byte[] serialize(Object obj) throws SerializerException {
        try {
            return fstConfiguration.asByteArray(obj);
        }catch (Exception e){
            throw  new SerializerException(StrUtil.format("序列化失败 -> [{}]", ClassUtil.getClassName(obj,true)),e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws DeSerializerException {
        try {
            return fstConfiguration.asObject(bytes);
        }catch (Exception e){
            throw  new DeSerializerException(StrUtil.format("反序列化失败 -> [{}]", ClassUtil.getClassName(this,true)),e);
        }
    }
}
