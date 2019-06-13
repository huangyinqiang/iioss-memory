package net.iioss.memory.core.impl.ehcache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.iioss.memory.core.definition.ProcessMemory;
import net.iioss.memory.core.definition.ProcessMemoryListener;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.impl.ehcache
 * @Description: ehcache实现进程内存操作
 * @date 2019/6/9 22:36
 */
@Getter
@Accessors(chain = true)
public class EhCache implements ProcessMemory, CacheEventListener {

    private net.sf.ehcache.Cache cache;
    @Setter
    private ProcessMemoryListener listener;


    public Cache getCache() {
        return cache;
    }

    public EhCache setCache(Cache cache) {
        this.cache = cache;
        this.cache.getCacheEventNotificationService().registerListener(this);
        return this;
    }

    @Override
    public long getTimeToLiveSeconds() {
        return cache.getCacheConfiguration().getTimeToLiveSeconds();
    }

    @Override
    public long getMaxSize() {
        return cache.getCacheConfiguration().getMaxEntriesLocalHeap();
    }

    @Override
    public Object get(String key) {
        if (StrUtil.isBlank(key))
            return null;
        Element element = cache.get(key);
        Object obj= (element!= null)?element.getObjectValue():null;
        return (obj == null || obj.getClass().equals(Object.class))?null:(Serializable)element.getObjectValue();
    }

    @Override
    public Map<String, Object> get(String... keys) {
        Map<String, Object> results = CollectionUtil.newHashMap();
        cache.getAll(CollectionUtil.newArrayList(keys)).forEach((k,v)-> {
            if(ObjectUtil.isNotNull(v))
                results.put((String)k, v.getObjectValue());
        });
        return results;
    }

    @Override
    public void put(String key, Object value) {
        cache.put(new Element(key, value));
    }

    @Override
    public void put(Map<String, Object> memoryData) {
        List<Element> elems = CollectionUtil.newArrayList();
        memoryData.forEach((k,v) -> elems.add(new Element(k,v)));
        cache.putAll(elems);
    }

    @Override
    public Collection<String> getKeys() {
        return cache.getKeys();
    }

    @Override
    public void clear() {
        cache.removeAll();
    }

    @Override
    public void delete(String... keys) {
        cache.removeAll(CollectionUtil.newArrayList(keys));
    }

    @Override
    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {

    }

    @Override
    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {

    }

    @Override
    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {

    }

    @Override
    public void notifyElementExpired(Ehcache ehcache, Element element) {
        if(ObjectUtil.isNotNull(listener))
            listener.notify(cache.getName(), (String)element.getObjectKey());
    }

    @Override
    public void notifyElementEvicted(Ehcache ehcache, Element element) {

    }

    @Override
    public void notifyRemoveAll(Ehcache ehcache) {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;
    }

    @Override
    public void dispose() {

    }
}
