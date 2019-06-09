package net.iioss.memory.core.base;

import java.util.Collection;
import java.util.Map;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.base
 * @Description: 内存操作的基类
 * @date 2019/6/9 13:26
 */
public interface Memory {

    /**
     * 根据key获取内存中的值
     * @param key　 key
     * @return 　　 内存中的值
     */
    Object get(String key);

    /**
     * 一次性获取多个key的值
     * @param keys      内存中的key　
     * @return 　　  　 内存中的值
     */
    Map<String,Object> get(String ... keys);

    /**
     * 给内存中存入数据
     * @param key　　   内存中的key
     * @param value     内存中的值
     */
    void put(String key, Object value);

    /**
     * 给内存中一次性存入一堆数据
     * @param memoryData key为内存中的key，值为内存中的值
     */
    void put(Map<String, Object> memoryData);

    /**
     * 获取内存中所有的key
     * @return 所有key
     */
    Collection<String> getKeys();

    /**
     * 清空整个内存
     */
    void clear();

    /**
     * 删除内存中的指定key
     * @param keys 指定的key集合
     */
    void delete(String ...  keys);

    /**
     * 是否存在一个key
     * @param key 指定的key
     * @return 是否存在的布尔值
     */
    default boolean isExist(String key) {
        return get(key) != null;
    }

    /**
     * TTL
     * @return 单位：秒
     */
    long getTimeToLiveSeconds();
}
