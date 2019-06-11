package net.iioss.memory.core;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import net.iioss.memory.core.definition.CommonMemory;
import net.iioss.memory.core.definition.MemorySupport;
import net.iioss.memory.core.definition.ProcessMemory;
import net.iioss.memory.core.bean.CacheObject;
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

    protected abstract void sendClearCmd(String nameSpace);
    protected abstract void senddeleteCmd(String nameSpace, String...keys);


    public CacheObject get(String nameSpace, String key, boolean...cacheNullObject)  {

        if(closed)
            throw new IllegalStateException("内存操作类已经关闭");

        CacheObject obj = new CacheObject(nameSpace, key, MemoryLevel.ONE,null);
        obj.setValue(admin.getProcessMemory(nameSpace).get(key));
        if(ObjectUtil.isNotNull(obj.getRealValue()))
            return obj;

        String lock_key = key + '%' + nameSpace;
        synchronized (locks.computeIfAbsent(lock_key, v -> new Object())) {
            obj.setValue(admin.getProcessMemory(nameSpace).get(key));
            if(ObjectUtil.isNotNull(obj.getRealValue()))
                return obj;

            try {
                obj.setLevel(MemoryLevel.TWO);
                obj.setValue(admin.getCommonMemory(nameSpace).get(key));
                if (ObjectUtil.isNotNull(obj.getRealValue())) {
                    admin.getCommonMemory(nameSpace).put(key, obj.getRealValue());
                }else {
                    boolean cacheNull = (cacheNullObject.length > 0) ? cacheNullObject[0] : true;
                    if (cacheNull)
                        set(nameSpace, key, new NullValue(), true);
                }
            } finally {
                locks.remove(lock_key);
            }
        }

        return obj;
    }



    public CacheObject get(String nameSpace, String key, Function<String, Object> loader, boolean...cacheNullObject) {

        if(closed)
            throw new IllegalStateException("内存操作类已经关闭");

        CacheObject cache = get(nameSpace, key, false);

        if (cache.getRealValue() != null)
            return cache ;

        String lock_key = key + '@' + nameSpace;
        synchronized (locks.computeIfAbsent(lock_key, v -> new Object())) {
            cache = get(nameSpace, key, false);

            if (cache.getRealValue() != null)
                return cache ;

            try {
                Object obj = loader.apply(key);
                boolean cacheNull = (cacheNullObject.length>0)?cacheNullObject[0]: true;
                set(nameSpace, key, obj, cacheNull);
                cache = new CacheObject(nameSpace, key,MemoryLevel.THREE, obj);
            } finally {
                locks.remove(lock_key);
            }
        }
        return cache;
    }


    public Map<String, CacheObject> get(String nameSpace, Collection<String> keys)  {

        if(closed)
            throw new IllegalStateException("内存操作类已经关闭");

        final Map<String, Object> objs = admin.getProcessMemory(nameSpace).get((String[]) keys.toArray());
        List<String> level2Keys = keys.stream().filter(k -> !objs.containsKey(k) || objs.get(k) == null).collect(Collectors.toList());
        Map<String, CacheObject> results = objs.entrySet().stream().filter(p -> p.getValue() != null).collect(
                Collectors.toMap(
                        p -> p.getKey(),
                        p -> new CacheObject(nameSpace, p.getKey(), MemoryLevel.ONE, p.getValue())
                )
        );

        Map<String, Object> objs_level2 = admin.getCommonMemory(nameSpace).get((String[]) level2Keys.toArray());
        objs_level2.forEach((k,v) -> {
            results.put(k, new CacheObject(nameSpace, k, MemoryLevel.TWO, v));
            if (v != null)
                admin.getProcessMemory(nameSpace).put(k, v);
        });

        return results;
    }


    public Map<String, CacheObject> get(String nameSpace, Collection<String> keys, Function<String, Object> loader, boolean...cacheNullObject)  {

        if(closed)
            throw new IllegalStateException("内存操作类已经关闭");

        Map<String, CacheObject> results = get(nameSpace, keys);
        results.entrySet().stream().filter(e -> e.getValue().getRealValue() == null).forEach( e -> {
            String lock_key = e.getKey() + '@' + nameSpace;
            synchronized (locks.computeIfAbsent(lock_key, v -> new Object())) {
                CacheObject cache = get(nameSpace, e.getKey(), false);
                if(cache.getRealValue() == null) {
                    try {
                        Object obj = loader.apply(e.getKey());
                        boolean cacheNull = (cacheNullObject.length>0)?cacheNullObject[0]: true;
                        set(nameSpace, e.getKey(), obj, cacheNull);
                        e.getValue().setValue(obj);
                        e.getValue().setLevel(MemoryLevel.THREE);
                    } finally {
                        locks.remove(lock_key);
                    }
                }
                else {
                    e.setValue(cache);
                }
            }
        });
        return results;
    }

    public boolean exists(String nameSpace, String key) {
        return check(nameSpace, key) > 0;
    }

    public int check(String nameSpace, String key) {
        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        if(admin.getProcessMemory(nameSpace).isExist(key))
            return 1;
        if(admin.getCommonMemory(nameSpace).isExist(key))
            return 2;
        return 0;
    }


    public void set(String region, String key, Object value) {
        set(region, key, value, true);
    }


    public void set(String nameSpace, String key, Object value, boolean cacheNullObject) {

        if (!cacheNullObject && value == null)
            return ;

        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        try {
            ProcessMemory level1 = admin.getProcessMemory(nameSpace);
            level1.put(key, (value==null && cacheNullObject)?new NullValue():value);
            CommonMemory level2 = admin.getCommonMemory(nameSpace);
            level2.put(key, (value==null && cacheNullObject)?new NullValue():value, level1.getTimeToLiveSeconds());
        } finally {
            this.senddeleteCmd(nameSpace, key);//清除原有的一级缓存的内容
        }
    }


    public void set(String nameSpace, String key, Object value, long timeToLiveInSeconds ) {
        set(nameSpace, key, value, timeToLiveInSeconds, true);
    }


    public void set(String nameSpace, String key, Object value, long timeToLiveInSeconds, boolean cacheNullObject) {

        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        if (!cacheNullObject && value == null)
            return ;

        if(timeToLiveInSeconds <= 0)
            set(nameSpace, key, value, cacheNullObject);
        else {
            try {
                admin.getProcessMemory(nameSpace, timeToLiveInSeconds).put(key, (value==null && cacheNullObject)?new NullValue():value);
                CommonMemory level2 = admin.getCommonMemory(nameSpace);
                level2.put(key, (value==null && cacheNullObject)?new NullValue():value, timeToLiveInSeconds);
            } finally {
                this.senddeleteCmd(nameSpace, key);//清除原有的一级缓存的内容
            }
        }
    }


    public void set(String nameSpace, Map<String, Object> elements){
        set(nameSpace, elements, true);
    }


    public void set(String nameSpace, Map<String, Object> elements, boolean cacheNullObject)  {

        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        try {
            if (cacheNullObject && elements.containsValue(null)) {
                Map<String, Object> newElems = new HashMap<>();
                newElems.putAll(elements);
                newElems.forEach((k,v) -> {
                    if (v == null)
                        newElems.put(k,new NullValue());
                });
                ProcessMemory level1 = admin.getProcessMemory(nameSpace);
                level1.put(newElems);
                admin.getCommonMemory(nameSpace).put(newElems, level1.getTimeToLiveSeconds());
            }
            else {
                ProcessMemory level1 = admin.getProcessMemory(nameSpace);
                level1.put(elements);
                admin.getCommonMemory(nameSpace).put(elements, level1.getTimeToLiveSeconds());
            }
        } finally {
            //广播
            this.senddeleteCmd(nameSpace, elements.keySet().stream().toArray(String[]::new));
        }
    }

    public void set(String region, Map<String, Object> elements, long timeToLiveInSeconds){
        set(region, elements, timeToLiveInSeconds, true);
    }


    public void set(String region, Map<String, Object> elements, long timeToLiveInSeconds, boolean cacheNullObject)  {

        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        if(timeToLiveInSeconds <= 0)
            set(region, elements, cacheNullObject);
        else {
            try {
                if (cacheNullObject && elements.containsValue(null)) {
                    Map<String, Object> newElems = new HashMap<>();
                    newElems.putAll(elements);
                    newElems.forEach((k,v) -> {
                        if (v == null)
                            newElems.put(k, new NullValue());
                    });
                    admin.getProcessMemory(region, timeToLiveInSeconds).put(newElems);
                    admin.getCommonMemory(region).put(newElems, timeToLiveInSeconds);
                }
                else {
                    admin.getProcessMemory(region, timeToLiveInSeconds).put(elements);
                    admin.getCommonMemory(region).put(elements, timeToLiveInSeconds);
                }
            } finally {
                //广播
                this.senddeleteCmd(region, elements.keySet().stream().toArray(String[]::new));
            }
        }
    }


    public void evict(String region, String...keys)  {

        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        try {
            //先清比较耗时的二级缓存，再清一级缓存
            admin.getCommonMemory(region).delete(keys);
            admin.getProcessMemory(region).delete(keys);
        } finally {
            this.senddeleteCmd(region, keys); //发送广播
        }
    }

    public void clear(String region)  {

        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        try {
            //先清比较耗时的二级缓存，再清一级缓存
            admin.getCommonMemory(region).clear();
            admin.getProcessMemory(region).clear();
        }finally {
            this.sendClearCmd(region);
        }
    }


    public Collection<NameSpace> getNameSpaces() {
        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        return admin.getNameSpaces();
    }


    public void removeRegion(String region) {
        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        admin.getProcessMemorySupport().removeCache(region);
    }


    public Collection<String> keys(String region)  {
        if(closed)
            throw new IllegalStateException("CacheChannel closed");

        Set<String> keys = new HashSet<>();
        keys.addAll(admin.getProcessMemory(region).getKeys());
        keys.addAll(admin.getCommonMemory(region).getKeys());
        return keys;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    public MemorySupport getProcessMemorySupport() {
        return admin.getProcessMemorySupport();
    }

    public MemorySupport getCommonMemorySupport() {
        return admin.getCommonMemorySupport();
    }
}
