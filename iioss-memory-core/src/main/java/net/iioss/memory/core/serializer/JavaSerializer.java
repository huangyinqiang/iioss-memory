package net.iioss.memory.core.serializer;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import net.iioss.memory.core.exception.DeSerializerException;
import net.iioss.memory.core.exception.SerializerException;

import java.io.*;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.serializer
 * @Description: java序列化
 * @date 2019/6/9 13:39
 */
public class JavaSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) throws SerializerException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(obj);
            return outputStream.toByteArray();
        }catch (Exception e){
            throw  new SerializerException(StrUtil.format("序列化失败 -> [{}]", ClassUtil.getClassName(obj,true)),e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws DeSerializerException {
        try {
            if (ObjectUtil.isEmpty(bytes))
                return null;
            return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        } catch (Exception e) {
            throw  new DeSerializerException(StrUtil.format("反序列化失败 -> [{}]", ClassUtil.getClassName(this,true)),e);
        }
    }
}
