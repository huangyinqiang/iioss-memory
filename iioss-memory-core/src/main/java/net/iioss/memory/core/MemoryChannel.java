package net.iioss.memory.core;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import net.iioss.memory.core.constant.Type;
import net.iioss.memory.core.definition.CommonMemory;
import net.iioss.memory.core.definition.MemorySupport;
import net.iioss.memory.core.definition.ProcessMemory;
import net.iioss.memory.core.bean.MemoryObject;
import net.iioss.memory.core.bean.NameSpace;
import net.iioss.memory.core.bean.NullValue;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.constant.MemoryLevel;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory
 * @Description: 内存频道
 * @date 2019/6/6 3:27
 */
@Data
public abstract class MemoryChannel implements AutoCloseable,Closeable {
    private static final Map<String, Object> locks = new ConcurrentHashMap<>();
    private Config config;
    private MemoryAdmin admin;
    private boolean closed;

    public MemoryChannel() {
        this.config = Singleton.get(Config.class);
        this.admin =  Singleton.get(MemoryAdmin.class);
        this.closed = false;
    }


    /**
     * 发布清空命令
     * @param nameSpace　名称空间
     */
    protected abstract void sendClearCmd(String nameSpace);

    /**
     * 发布删除命令
     * @param nameSpace　名称空间
     * @param keys　　　　keys
     */
    protected abstract void senddeleteCmd(String nameSpace, String...keys);


    /**
     * 获取内存中的数据
     * @param nameSpace　名称空间
     * @param key　　　　　key
     * @param isCanNull 　是否可以为null
     * @return 内存数据对象　
     */
    public MemoryObject get(String nameSpace, String key, boolean isCanNull)  {
        if(closed)
            throw new IllegalStateException("内存操作类已经关闭");

        MemoryObject memoryValue = new MemoryObject(nameSpace, key, Type.PROCESS_MEMORY,null)
                .setValue(admin.getProcessMemory(nameSpace).get(key));
        //如果从进程内存中拿到数据直接返回
        if(ObjectUtil.isNotNull(memoryValue.getValue()))
            return memoryValue;

        String lock_key = key + '%' + nameSpace;
        synchronized (locks.computeIfAbsent(lock_key, v -> new Object())) {
            memoryValue.setValue(admin.getProcessMemory(nameSpace).get(key));
            if(ObjectUtil.isNotNull(memoryValue.getValue()))
                return memoryValue;

            try {
                memoryValue.setType(Type.COMMON_MEMORY).setValue(admin.getCommonMemory(nameSpace).get(key));
                //如果从进程外拿到数据，存入一份到进程内存中
                if (ObjectUtil.isNotNull(memoryValue.getValue())) {
                    admin.getProcessMemory(nameSpace).put(key, memoryValue.getValue());
                }else {
                    if (isCanNull)
                        put(nameSpace, key, new NullValue(), true);
                }
            } finally {
                locks.remove(lock_key);
            }
        }

        return memoryValue;
    }




    /**
     *批量获取内存中的数据
     * @param nameSpace　名称空间
     * @param keys　　　　　keys
     * @return 内存数据对象集合　
     */
    public Map<String, MemoryObject> get(String nameSpace, String... keys)  {
        if(closed)
            throw new IllegalStateException("内存操作类已经关闭");

        //进程内存数据
        final Map<String, Object> objectMap = admin.getProcessMemory(nameSpace).get(keys);
        Map<String, MemoryObject> results = objectMap.entrySet().stream().filter(p -> p.getValue() != null).collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        p -> new MemoryObject(nameSpace, p.getKey(), Type.PROCESS_MEMORY, p.getValue())
                )
        );
        //进程外内存数据
        Map<String, Object> commonDataMap = admin.getCommonMemory(nameSpace).get(CollectionUtil.newArrayList(keys).stream().filter(k -> !objectMap.containsKey(k) || objectMap.get(k) == null).toArray(String[]::new));
        commonDataMap.forEach((k,v) -> {
            results.put(k, new MemoryObject(nameSpace, k, Type.COMMON_MEMORY, v));
            if (v != null)
                admin.getProcessMemory(nameSpace).put(k, v);
        });
        return results;
    }


    /**
     * 判断是否存在
     * @param nameSpace 名称空间
     * @param key　　　　　key
     * @return　是否存在
     */
    public boolean exists(String nameSpace, String key) {
        Type check = getType(nameSpace, key);
        return check == Type.PROCESS_MEMORY
                || check == Type.COMMON_MEMORY;
    }


    /**
     * 获取当前数据的内存级别
     * @param nameSpace　名称空间
     * @param key　　　　　主键
     * @return 类型　
     */
    public Type getType(String nameSpace, String key) {
        if(closed)
            throw new IllegalStateException("缓存通道已经关闭");

        if(admin.getProcessMemory(nameSpace).isExist(key))
            return Type.PROCESS_MEMORY;
        if(admin.getCommonMemory(nameSpace).isExist(key))
            return Type.COMMON_MEMORY;
        return Type.NONE;
    }

    /*存入单条数据　　　start*/
    /**
     * 存入内存数据
     * @param nameSpace　名称空间
     * @param key　　　　　主键
     * @param value　　　　数据
     */
    public void put(String nameSpace, String key, Object value) {
        put(nameSpace, key, value, true);
    }

    /**
     * 存入内存数据
     * @param nameSpace 名称空间
     * @param key　　　　　key
     * @param value      内存数据
     * @param isCanNull　是否可以为null
     */
    public void put(String nameSpace, String key, Object value, boolean isCanNull) {
        if(closed)
            throw new IllegalStateException("缓存频道已经关闭");
        if (!isCanNull && value == null)
            return ;
        try {
            ProcessMemory processMemory = admin.getProcessMemory(nameSpace);
            processMemory.put(key, (value==null && isCanNull)?new NullValue():value);
            CommonMemory cMemory = admin.getCommonMemory(nameSpace);
            cMemory.put(key,(value==null && isCanNull)?new NullValue():value, processMemory.getTimeToLiveSeconds());
        } finally {
            this.senddeleteCmd(nameSpace, key);
        }
    }

    /**
     * 存入内存数据
     * @param nameSpace　名称空间
     * @param key　　　　　主键
     * @param timeToLiveInSeconds      生命周期
     * @param value　　　　数据
     */
    public void put(String nameSpace, String key, Object value,long timeToLiveInSeconds) {
        put(nameSpace, key, value, timeToLiveInSeconds,true);
    }

    /**
     * 存入内存数据
     * @param nameSpace 名称空间
     * @param key　　　　　key
     * @param value      内存数据
     * @param timeToLiveInSeconds      生命周期
     * @param isCanNull　是否可以为null
     */
    public void put(String nameSpace, String key, Object value, long timeToLiveInSeconds, boolean isCanNull) {
        if(closed)
            throw new IllegalStateException("缓存通道已经关闭");
        if (!isCanNull && value == null)
            return ;
        if(timeToLiveInSeconds <= 0)
            put(nameSpace, key, value, isCanNull);
        else {
            try {
                admin.getProcessMemory(nameSpace, timeToLiveInSeconds).put(key, (value==null && isCanNull)?new NullValue():value);
                CommonMemory commonMemory = admin.getCommonMemory(nameSpace);
                commonMemory.put(key, (value==null && isCanNull)?new NullValue():value, timeToLiveInSeconds);
            } finally {
                this.senddeleteCmd(nameSpace, key);
            }
        }
    }
    /*存入单条数据　　 　end*/



    /*存入多条数据　　 　start*/
    /**
     * 批量存入数据
     * @param nameSpace　名称空间
     * @param values　　　　数据对象
     * @param isCanNull　是否可以为null
     */
    public void put(String nameSpace, Map<String, Object> values, boolean isCanNull)  {
        if(closed)
            throw new IllegalStateException("缓存频道已经关闭");

        try {
            if (isCanNull && values.containsValue(null)) {
                Map<String, Object> objectMap = CollectionUtil.newHashMap();
                objectMap.putAll(values);
                objectMap.forEach((k,v) -> {
                    if (v == null)
                        objectMap.put(k,new NullValue());
                });
                ProcessMemory processMemory = admin.getProcessMemory(nameSpace);
                processMemory.put(objectMap);
                admin.getCommonMemory(nameSpace).put(objectMap, processMemory.getTimeToLiveSeconds());
            }else {
                ProcessMemory processMemory = admin.getProcessMemory(nameSpace);
                processMemory.put(values);
                admin.getCommonMemory(nameSpace).put(values, processMemory.getTimeToLiveSeconds());
            }
        } finally {
            this.senddeleteCmd(nameSpace, values.keySet().toArray(new String[0]));
        }
    }


    /**
     * 带生命周期的设置
     * @param nameSpace　名称空间
     * @param values    内存数据
     * @param timeToLiveInSeconds　生命周期
     */
    public void put(String nameSpace, Map<String, Object> values, long timeToLiveInSeconds){
        put(nameSpace, values, timeToLiveInSeconds, true);
    }

    /**
     * 带生命周期的设置
     * @param nameSpace　名称空间
     * @param values    内存数据
     * @param timeToLiveInSeconds　生命周期
     */
    public void put(String nameSpace, Map<String, Object> values, long timeToLiveInSeconds, boolean isCanNull)  {
        if(closed)
            throw new IllegalStateException("内存通道已经关闭");

        if(timeToLiveInSeconds <= 0)
            put(nameSpace, values, isCanNull);
        else {
            try {
                if (isCanNull && values.containsValue(null)) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.putAll(values);
                    objectMap.forEach((k,v) -> {
                        if (v == null)
                            objectMap.put(k, new NullValue());
                    });
                    admin.getProcessMemory(nameSpace, timeToLiveInSeconds).put(objectMap);
                    admin.getCommonMemory(nameSpace).put(objectMap, timeToLiveInSeconds);
                } else {
                    admin.getProcessMemory(nameSpace, timeToLiveInSeconds).put(values);
                    admin.getCommonMemory(nameSpace).put(values, timeToLiveInSeconds);
                }
            } finally {
                this.senddeleteCmd(nameSpace, values.keySet().toArray(new String[0]));
            }
        }
    }


    /**
     * 删除内存数据
     * @param nameSpace　名称空间
     * @param keys　　　　　keys
     */
    public void delete(String nameSpace, String...keys)  {
        if(closed)
            throw new IllegalStateException("内存通道已经关闭");
        try {
            admin.getCommonMemory(nameSpace).delete(keys);
            admin.getProcessMemory(nameSpace).delete(keys);
        } finally {
            this.senddeleteCmd(nameSpace, keys);
        }
    }


    /**
     * 清空指定的内存数据
     * @param nameSpace　名称空间
     */
    public void clear(String nameSpace)  {
        if(closed)
            throw new IllegalStateException("内存通道已经关闭");

        try {
            admin.getCommonMemory(nameSpace).clear();
            admin.getProcessMemory(nameSpace).clear();
        }finally {
            this.sendClearCmd(nameSpace);
        }
    }


    /**
     * 获取所有的名称空间
     * @return 所有的名称空间
     */
    public Collection<NameSpace> getNameSpaces() {
        if(closed)
            throw new IllegalStateException("内存通道已经关闭");
        return admin.getNameSpaces();
    }


    /**
     * 移除一个名称空间
     * @param nameSpace　名称空间
     */
    public void removeRegion(String nameSpace) {
        if(closed)
            throw new IllegalStateException("内存通道已经关闭");
        admin.getProcessMemorySupport().removeCache(nameSpace);
    }


    /**
     * 获取当前名称空间下的所有key
     * @param nameSpace 名称空间
     * @return
     */
    public Collection<String> keys(String nameSpace)  {
        if(closed)
            throw new IllegalStateException("内存通道已经关闭");
        Set<String> keys = CollectionUtil.newHashSet();
        keys.addAll(admin.getProcessMemory(nameSpace).getKeys());
        keys.addAll(admin.getCommonMemory(nameSpace).getKeys());
        return keys;
    }


    /**
     * 关闭缓存通道
     */
    @Override
    public void close() {
        this.closed = true;
    }


    /**
     * 获取进程内存的支持类
     * @return 内存的支持类
     */
    public MemorySupport getProcessMemorySupport() {
        return admin.getProcessMemorySupport();
    }

    /**
     * 获取进程内存外内存的支持类
     * @return 内存的支持类
     */
    public MemorySupport getCommonMemorySupport() {
        return admin.getCommonMemorySupport();
    }
}
