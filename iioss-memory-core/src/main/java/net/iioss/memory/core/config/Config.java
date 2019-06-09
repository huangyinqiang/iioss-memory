package net.iioss.memory.core.config;

import cn.hutool.core.lang.Singleton;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.bean.ConfigEntry;
import net.iioss.memory.core.definition.*;
import java.util.Map;

import static net.iioss.memory.core.definition.NameDefinition.*;

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
    public static String coreConfigFile= NameDefinition.CORE_FILE;
    private Map<Type,ConfigEntry> fileConfig;


    /**
     * 启动config
     * @return 当前项目配置类
     */
    public static void start() {
        initConfig();
    }

    /**
     * 启动config
     * @param configFileName 配置文件的名称
     * @return 当前项目配置类
     */
    public static void start(String configFileName) {
        setConfig(configFileName);
        initConfig();
    }

    /**
     * 加载核心的配置文件
     * @param configFileName 配置文件路径
     * @return 核心配置类
     */
    public synchronized static void setConfig(String configFileName){
        coreConfigFile=configFileName;
    }

    /**
     * 对核心的配置文件进行初始化
     * @return 返回核心的配置类
     */
    private static void initConfig() {
        Config iiossMemoryConfig = Singleton.get(Config.class);
        iiossMemoryConfig.fileConfig.put(Type.SERIALIZER,ConfigEntry.init(new ConfigEntry() {
                    @Override
                    public String getKey() {
                        return SERIALIZER_KEY;
                    }
                },
                e->e.setType(Type.SERIALIZER)
                   .setSerializerType(SerializerType.FST)
                   .setName(SerializerType.FST.getName())
        ));
        iiossMemoryConfig.fileConfig.put(Type.BROADCAST,ConfigEntry.init(new ConfigEntry() {
                     @Override
                     public String getKey() {
                         return BROADCAST;
                     }
                 },
                e->e.setType(Type.BROADCAST)
                        .setBroadcastType(BroadcastType.REDIS)
                        .setName(BroadcastType.REDIS.getName())
                ));
        iiossMemoryConfig.fileConfig.put(Type.PROCESS_MEMORY,ConfigEntry.init(new ConfigEntry() {
                      @Override
                      public String getKey() {
                          return PROCESS_MEMORY;
                      }
                },
                e->e.setType(Type.PROCESS_MEMORY)
                        .setMemoryType(MemoryType.ENCACHE)
                        .setName(MemoryType.ENCACHE.getName()).setMemoryLevel(MemoryLevel.ONE)
        ));
        iiossMemoryConfig.fileConfig.put(Type.COMMON_MEMORY,ConfigEntry.init(new ConfigEntry() {
                     @Override
                     public String getKey() {
                         return COMMON_MEMORY;
                     }
                 },
                e->e.setType(Type.COMMON_MEMORY)
                        .setMemoryType(MemoryType.REDIS)
                        .setName(MemoryType.REDIS.getName()).setMemoryLevel(MemoryLevel.TWO)
        ));
    }

}
