package net.iioss.memory.core.impl.ehcache;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.definition.Memory;
import net.iioss.memory.core.definition.MemorySupport;
import net.iioss.memory.core.definition.ProcessMemoryListener;
import net.iioss.memory.core.bean.NameSpace;
import net.iioss.memory.core.constant.MemoryLevel;
import net.iioss.memory.core.constant.MemoryType;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static net.iioss.memory.core.constant.NameDefinition.*;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.impl.ehcache
 * @Description: ehcache支撑类
 * @date 2019/6/9 22:45
 */
@Slf4j
public class EhCacheSupport  implements MemorySupport {
    private CacheManager manager;
    private ConcurrentHashMap<String, EhCache> caches;
    @Override
    public String getName() {
        return MemoryType.ENCACHE.getName();
    }

    @Override
    public MemoryLevel getLevel() {
        return MemoryLevel.ONE;
    }

    @Override
    public Memory build(String nameSpace, ProcessMemoryListener listener) {
        return caches.computeIfAbsent(nameSpace, v -> {
            net.sf.ehcache.Cache cache = manager.getCache(nameSpace);
            if (cache == null) {
                manager.addCache(nameSpace);
                cache = manager.getCache(nameSpace);
                log.warn("沒有找到相应的配置 [{}]; 使用默认值 (存活时间:{} seconds).", nameSpace, cache.getCacheConfiguration().getTimeToLiveSeconds());
            }
            return new EhCache().setCache(cache).setListener(listener);
        });
    }

    @Override
    public Memory build(String nameSpace, long timeToLiveSeconds, ProcessMemoryListener listener) {
        EhCache ehcache = caches.computeIfAbsent(nameSpace, v -> {
            CacheConfiguration cfg = manager.getConfiguration().getDefaultCacheConfiguration().clone();
            cfg.setName(nameSpace);
            if(timeToLiveSeconds > 0) {
                cfg.setTimeToLiveSeconds(timeToLiveSeconds);
                cfg.setTimeToIdleSeconds(timeToLiveSeconds);
            }

            net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(cfg);
            manager.addCache(cache);
            log.info("启动 Ehcache nameSpace [{}] with TTL: {}", nameSpace, timeToLiveSeconds);
            return new EhCache().setCache(cache).setListener(listener);
        });

        if (ehcache.getTimeToLiveSeconds() != timeToLiveSeconds)
            throw new IllegalArgumentException(String.format("Region [%s] TTL %d not match with %d", nameSpace, ehcache.getTimeToLiveSeconds(), timeToLiveSeconds));

        return ehcache;
    }

    @Override
    public void removeCache(String region) {
        caches.remove(region);
        manager.removeCache(region);
    }


    @Override
    public Collection<NameSpace> getNameSpaces() {
        List<NameSpace> nameSpaces = CollectionUtil.newArrayList();
        caches.forEach((k,c) -> nameSpaces.add(new NameSpace(k, c.getMaxSize(), c.getTimeToLiveSeconds())));
        return nameSpaces;
    }

    @Override
    public void start(Properties props) {
        if (manager != null) {
            log.warn("正在重启EhCacheProvider");
            return;
        }
        String ehcacheName = (String) props.get(NAME);
        if (ehcacheName != null && ehcacheName.trim().length() > 0)
            manager = CacheManager.getCacheManager(ehcacheName);
        if (manager == null) {
            // 指定了配置文件路径? 加载之
            if (props.containsKey(CONFIGLOCATION)) {
                URL url = getClass().getClassLoader().getResource((String) props.get(CONFIGLOCATION));
                manager = CacheManager.newInstance(url);
            } else {
                // 加载默认实例
                manager = CacheManager.getInstance();
            }
        }
        caches = new ConcurrentHashMap<>();
    }

    @Override
    public void stop() {
        if (manager != null) {
            manager.shutdown();
            caches.clear();
            manager = null;
        }
    }
}
