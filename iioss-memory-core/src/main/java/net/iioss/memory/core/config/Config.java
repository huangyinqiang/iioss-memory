package net.iioss.memory.core.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.setting.Setting;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.bean.ConfigEntry;
import net.iioss.memory.core.constant.*;
import net.iioss.memory.core.util.SettingUtil;

import java.util.Map;

import static net.iioss.memory.core.constant.NameDefinition.*;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.config
 * @Description: IIOSSMemory配置解析类,用于加载配置文件，解析配置文件
 * @date 2019/6/9 15:05
 */
@Slf4j
@Data
@Accessors(chain = true)
public class Config {
    /**
     * 项目默认加载的配置文件
     */
    public static String coreConfigFile= NameDefinition.CORE_FILE;
    /**
     * 项目中加载解析的配置文件的各个bean
     */
    private Map<Type,ConfigEntry> fileConfig=CollectionUtil.newHashMap();



    /**
     * 加载配置文件
     * @return 当前项目配置类
     */
    public static Config loadConfig() {
        return initConfig();
    }


    /**
     * 加载配置文件
     * @param configFileName 配置文件的名称
     * @return 当前项目配置类
     */
    public static Config loadConfig(String configFileName) {
        setConfig(configFileName);
        return initConfig();
    }


    /**
     * 设置配置文件位置
     * @param configFileName 配置文件路径
     */
    public synchronized static void setConfig(String configFileName){
        coreConfigFile=configFileName;
    }


    /**
     * 对核心的配置文件进行初始化
     * @return 返回核心的配置类
     */
    private static Config initConfig() {
        //加载配置文件
        BeanUtil.copyProperties(SettingUtil.get(coreConfigFile), Singleton.get(Setting.class));
        Config config = Singleton.get(Config.class);
        //初始化序列化方案
        config.fileConfig.put(Type.SERIALIZER,ConfigEntry.init(new ConfigEntry(),
                e->e.setType(Type.SERIALIZER)
                   .setSerializerType(SerializerType.FST)
                   .setName(SerializerType.FST.getName())
        ));
        //初始化广播方案
        config.fileConfig.put(Type.BROADCAST,ConfigEntry.init(new ConfigEntry(),
                e->e.setType(Type.BROADCAST)
                        .setBroadcastType(BroadcastType.REDIS)
                        .setName(BroadcastType.REDIS.getName())
                ));
        //初始化进程内存方案
        config.fileConfig.put(Type.PROCESS_MEMORY,ConfigEntry.init(new ConfigEntry(),
                e->e.setType(Type.PROCESS_MEMORY)
                        .setMemoryType(MemoryType.ENCACHE)
                        .setName(MemoryType.ENCACHE.getName()).setMemoryLevel(MemoryLevel.ONE)
        ));
        //初始化进程外内存方案
        config.fileConfig.put(Type.COMMON_MEMORY,ConfigEntry.init(new ConfigEntry(),
                e->e.setType(Type.COMMON_MEMORY)
                        .setMemoryType(MemoryType.REDIS)
                        .setName(MemoryType.REDIS.getName()).setMemoryLevel(MemoryLevel.TWO)
        ));
        return config;
    }

}
