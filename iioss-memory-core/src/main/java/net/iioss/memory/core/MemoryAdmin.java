package net.iioss.memory.core;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.definition.*;
import net.iioss.memory.core.bean.ConfigEntry;
import net.iioss.memory.core.bean.NameSpace;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.constant.MemoryLevel;
import net.iioss.memory.core.constant.Type;
import net.iioss.memory.core.util.SettingUtil;
import net.sf.ehcache.CacheException;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static net.iioss.memory.core.constant.NameDefinition.CACHE_PRE;

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
    private Map<Type,MemorySupport> support=CollectionUtil.newHashMap();


    /**
     * 初始化内存管理器
     * @param listener 监听器
     * @return  内存管理器
     */
    public static MemoryAdmin init(ProcessMemoryListener listener) {
        MemoryAdmin memoryAdmin = Singleton.get(MemoryAdmin.class);
        Config config = Singleton.get(Config.class);
        memoryAdmin.listener = listener;
        //进程内存
        MemorySupport memorySupport = getMemorySupport(Type.PROCESS_MEMORY);
        memoryAdmin.support.put(Type.PROCESS_MEMORY,memorySupport);
        assert memorySupport != null;
        Map<String, String> mapByPrefix = SettingUtil.getMapByPrefix(config.getFileConfig().get(Type.PROCESS_MEMORY).getKey());
        memorySupport.start(mapByPrefix);
        log.info("进程内存的提供者 : {}", memorySupport.getClass().getName());

        //进程外内存
        MemorySupport support =getMemorySupport(Type.COMMON_MEMORY);
        memoryAdmin.support.put(Type.COMMON_MEMORY,support);
        assert support != null;
        support.start(SettingUtil.getMapByPrefix(config.getFileConfig().get(Type.COMMON_MEMORY).getKey()));
        log.info("进程外内存的提供者 : {}", support.getClass().getName());
        return memoryAdmin;
    }


    /**
     * 获取进程内存
     * @return 进程内存
     */
    private static MemorySupport getMemorySupport(Type type) {
        //获取配置bean
        Config config = Singleton.get(Config.class);
        ConfigEntry configEntry = config.getFileConfig()
                .values().parallelStream()
                .filter(e -> e.getType() == type).collect(Collectors.toList()).get(0);

        if (configEntry.getType()==Type.COMMON_MEMORY || configEntry.getType()==Type.PROCESS_MEMORY){
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
        support.get(Type.PROCESS_MEMORY).stop();
        support.get(Type.COMMON_MEMORY).stop();
    }


    /**
     * 获取进程内存的支持对象
     * @return 进程缓存的支持对象
     */
    public MemorySupport getProcessMemorySupport() {
        return support.get(Type.PROCESS_MEMORY);
    }


    /**
     * 获取进程外内存的支持对象
     * @return 进程外内存的支持对象
     */
    public MemorySupport getCommonMemorySupport() {
        return support.get(Type.COMMON_MEMORY);
    }


    /**
     * 根据名称空间获取内存实体
     * @param nameSpace　名称空间
     * @return 内存实体
     */
    public ProcessMemory getProcessMemory(String nameSpace) {
        return getProcessMemory(nameSpace,null);
    }


    /**
     * 根据名称空间和生命周期获取内存实体
     * @param nameSpace　名称空间
     * @param timeToLiveSeconds　生命周期
     * @return 内存实体
     */
    public ProcessMemory getProcessMemory(String nameSpace,Long timeToLiveSeconds) {
        MemorySupport memorySupport = support.get(Type.PROCESS_MEMORY);
        ProcessMemory memory=null;
        if (ObjectUtil.isNull(timeToLiveSeconds)){
            memory=(ProcessMemory)memorySupport.build(nameSpace,listener);
        }else {
            memory=(ProcessMemory)memorySupport.build(nameSpace,timeToLiveSeconds,listener);
        }
        return memory;
    }


    /**
     * 根据名称空间获取进程外内存实体
     * @param nameSpace　名称空间
     * @return 内存实体
     */
    public CommonMemory getCommonMemory(String nameSpace) {
        return getCommonMemory(nameSpace,null);
    }

    /**
     * 根据名称空间和生命周期获取进程外内存实体
     * @param nameSpace　名称空间
     * @param timeToLiveSeconds　生命周期
     * @return 内存实体
     */
    public CommonMemory getCommonMemory(String nameSpace,Long timeToLiveSeconds) {
        nameSpace=CACHE_PRE+nameSpace;
        MemorySupport memorySupport = support.get(Type.COMMON_MEMORY);
        CommonMemory memory=null;
        if (ObjectUtil.isNull(timeToLiveSeconds)){
            memory=(CommonMemory)memorySupport.build(nameSpace,listener);
        }else {
            memory=(CommonMemory)memorySupport.build(nameSpace,timeToLiveSeconds,listener);
        }
        return memory;
    }


    /**
     * 获取当前配置的所有名称空间
     * @return
     */
    public Collection<NameSpace> getNameSpaces() {
        return support.get(Type.PROCESS_MEMORY).getNameSpaces();
    }
}
