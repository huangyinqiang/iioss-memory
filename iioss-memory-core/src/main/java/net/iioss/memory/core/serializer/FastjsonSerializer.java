package net.iioss.memory.core.serializer;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.iioss.memory.core.constant.SerializerType;
import net.iioss.memory.core.exception.DeSerializerException;
import net.iioss.memory.core.exception.SerializerException;

public class FastjsonSerializer implements Serializer {

    @Override
    public String name() {
        return SerializerType.getSerializerTypeByClassName(ClassUtil.getClassName(this,true)).getName();
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            return JSON.toJSONString(obj, SerializerFeature.WriteClassName).getBytes();
        }catch (Exception e){
            throw  new SerializerException(StrUtil.format("序列化对象时发生失败 -> [{}]", ClassUtil.getClassName(obj,true)),e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) {
        try {
            return JSON.parse(new String(bytes), Feature.SupportAutoType);
        }catch (Exception e){
            throw  new DeSerializerException(StrUtil.format("反序列化发生失败 -> [{}]", ClassUtil.getClassName(this,true)),e);
        }
    }

}
