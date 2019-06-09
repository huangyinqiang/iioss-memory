package net.iioss.memory.core;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.base.*;
import net.iioss.memory.core.bean.ConfigEntry;
import net.iioss.memory.core.bean.NameSpace;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.definition.MemoryLevel;
import net.iioss.memory.core.definition.Type;
import net.iioss.memory.core.exception.MemoryException;
import net.iioss.memory.core.util.PropertiesUtil;
import net.sf.ehcache.CacheException;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core
 * @Description: 内存管理器
 * @date 2019/6/9 22:16
 */
@Slf4j
public class MemoryAdmin {

    /**
    * 监听事件*/
    private ProcessMemoryListener listener;

    /**
     * 内存map
     */
    private Map<MemoryLevel,MemorySupport> support=CollectionUtil.newHashMap();


    /**
     * 初始化内存管理器
     * @param listener 监听器
     * @return  内存管理器
     */
    public static MemoryAdmin init(ProcessMemoryListener listener) {
        MemoryAdmin memoryAdmin = Singleton.get(MemoryAdmin.class);
        Config config = Singleton.get(Config.class);
        memoryAdmin.listener = listener;
        MemorySupport memorySupport = getMemorySupport(MemoryLevel.ONE);
        memoryAdmin.support.put(MemoryLevel.ONE,memorySupport);
        memorySupport.start(PropertiesUtil.getSubProperties(config.getFileConfig().get(Type.PROCESS_MEMORY).getName(),Singleton.get(Properties.class)));
        log.info("1级缓存的提供者 : {}", memorySupport.getClass().getName());
        MemorySupport memorySupport2 =getMemorySupport(MemoryLevel.TWO);
        memoryAdmin.support.put(MemoryLevel.TWO,memorySupport2);
        memorySupport2.start(PropertiesUtil.getSubProperties(config.getFileConfig().get(Type.PROCESS_MEMORY).getName(),Singleton.get(Properties.class)));
        log.info("2级缓存的提供者 : {}", memorySupport2.getClass().getName());
        return memoryAdmin;
    }

    /**
     * 获取进程内存
     * @return 进程内存
     */
    private static MemorySupport getMemorySupport(MemoryLevel memoryLevel) {
        Config config = Singleton.get(Config.class);
        ConfigEntry configEntry = config.getFileConfig()
                .values()
                .parallelStream()
                .filter(e -> e.getMemoryLevel() == memoryLevel).collect(Collectors.toList()).get(0);
        if (configEntry.getType()==Type.COMMON_MEMORY
                || configEntry.getType()==Type.PROCESS_MEMORY){
            String className = configEntry.getMemoryType().getClassName();
            try {
                if (StrUtil.isNotEmpty(className)){
                    return (MemorySupport)Class.forName(className).newInstance();
                }else {
                    return (MemorySupport)Class.forName(configEntry.getName()).newInstance();

                }
            } catch (Exception e) {
                throw new CacheException("初始化MemorySupport失败", e);
            }


        }
        return null;
    }

    /**
     * 关闭缓存
     */
    public void shutdown() {
        support.get(MemoryLevel.ONE).stop();
        support.get(MemoryLevel.TWO).stop();
    }

    public MemorySupport getProcessMemorySupport() {
        return support.get(MemoryLevel.ONE);
    }

    public MemorySupport getCommonMemorySupport() {
        return support.get(MemoryLevel.TWO);
    }


    public ProcessMemory getProcessMemory(String nameSpace) {
        return (ProcessMemory)support.get(MemoryLevel.ONE).build(nameSpace, listener);
    }

    public ProcessMemory getProcessMemory(String nameSpace,long timeToLiveSeconds) {
        return (ProcessMemory)support.get(MemoryLevel.ONE).build(nameSpace, timeToLiveSeconds,listener);
    }

    public CommonMemory getCommonMemory(String nameSpace) {
        return (CommonMemory)support.get(MemoryLevel.TWO).build(nameSpace, listener);
    }

    public CommonMemory getCommonMemory(String nameSpace,long timeToLiveSeconds) {
        return (CommonMemory)support.get(MemoryLevel.TWO).build(nameSpace, timeToLiveSeconds,listener);
    }

    public Collection<NameSpace> getNameSpaces() {
        return support.get(MemoryLevel.ONE).getNameSpaces();
    }
}
