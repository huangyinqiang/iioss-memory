package net.iioss.memory.core.base;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import net.iioss.memory.core.exception.DeSerializerException;
import net.iioss.memory.core.exception.SerializerException;
import net.iioss.memory.core.serializer.SerializationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.base
 * @Description: 进程外内存操作的基类
 *                 需要经过序列化
 * @date 2019/6/9 13:33
 */
public interface CommonMemory extends Memory {
    Logger log = LoggerFactory.getLogger(CommonMemory.class);

    /**
     * 根据key获取内存中的字节流数组
     * @param key　对应的key
     * @return 　　内存中的值
     */
    byte[] getBytes(String key);

    /**
     * 根据key获取内存中多个字节流数组
     * @param keys 对应的集合key
     * @return 　　内存中的值
     */
    Collection<byte[]> getBytes(String ... keys);

    /**
     * 给内存中设置值
     * @param key　　key
     * @param value  对应的值
     */
    void setBytes(String key, byte[] value);

    /**
     * 给内存中一次性设置多个值
     * @param memoryData key为内存中的key，值为内存中的值
     */
    void setBytes(Map<String,byte[]> memoryData);

    /**
     * 给内存中设置带生命周期的值
     * @param key　　　key
     * @param value　　值
     * @param timeToLiveSeconds　生命周期,单位：秒
     */
    default void setBytes(String key, byte[] value, long timeToLiveSeconds){
        setBytes(key, value);
    }

    /**
     * 给内存中一次性设置多个带生命周期的值
     * @param objectMap　　　值
     * @param timeToLiveSeconds　生命周期,单位：秒
     */
    default void setBytes(Map<String,byte[]> objectMap, long timeToLiveSeconds) {
        setBytes(objectMap);
    }

    @Override
    default Object get(String key) {
        try {
            return SerializationAdapter.deserialize(getBytes(key));
        } catch (DeSerializerException e) {
            log.warn("反序列化失败  key:" + key + ",message: " + e.getMessage());
            delete(key);
            return null;
        }
    }

    @Override
    default Map<String, Object> get(String ...  keys) {
        Map<String, Object> results = new HashMap<>();
        if(ObjectUtil.isNotEmpty(keys)) {
            Map<String, byte[]> map = IterUtil.toMap(CollectionUtil.newArrayList(keys), getBytes(keys));
            CollectionUtil.forEach(map,(e,f,i)->{
                try {
                    results.put(e,SerializationAdapter.deserialize(f));
                }catch (DeSerializerException de) {
                    log.warn("反序列化失败  key:" + e + ",message: " + de.getMessage());
                    delete(e);
                    throw new DeSerializerException(de);
                }
            });
        }
        return results;
    }

    @Override
    default void put(String key, Object value) {
        try {
            setBytes(key, SerializationAdapter.serialize(value));
        } catch (SerializerException e) {
            log.warn("序列化失败  key:" + key + ",message: " + e.getMessage());
            throw new SerializerException(e);
        }
    }

    @Override
    default void put(Map<String, Object> memoryData) {
        if(MapUtil.isNotEmpty(memoryData)){
            setBytes(memoryData.entrySet()
                    .parallelStream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, p->SerializationAdapter.serialize(p.getValue()))));
        }
    }

    default void put(String key, Object value, long timeToLiveInSeconds) {
        try {
            setBytes(key, SerializationAdapter.serialize(value), timeToLiveInSeconds);
        } catch (SerializerException e) {
            log.warn("序列化失败 key:" + key + ",message: " + e.getMessage());
            throw new SerializerException(e);
        }
    }

    default void put(Map<String, Object> memoryData, long timeToLiveInSeconds) {
        put(memoryData);
    }

    /**
     * 是否支持内存数据生命周期设置
     * @return
     */
    default boolean isSupportTimeToLiveSeconds() {
        return false;
    }

}
