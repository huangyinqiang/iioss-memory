package net.iioss.memory.core;

import cn.hutool.core.lang.Singleton;
import net.iioss.memory.core.config.Config;
import net.sf.ehcache.CacheException;

import java.io.IOException;
import java.util.Properties;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core
 * @Description: 核心类
 * @date 2019/6/9 13:25
 */
public class IIOSSMemory {

    public static MemoryChannel getChannel(){
        return MemoryBuilder.init(Singleton.get(Config.class)).getChannel();
    }

    /**
     * 关闭 J2Cache
     */
    public static void close() {
        MemoryBuilder.init(Singleton.get(Config.class)).close();
    }
}
