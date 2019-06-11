package net.iioss.memory.core.definition;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.base
 * @Description: 进程内存数据监听器
 *                          用于更新进程内存数据过期
 * @date 2019/6/9 14:24
 */
public interface ProcessMemoryListener {

    /**
     * 更新进程内存过期数据
     * @param nameSpace  名称空间
     * @param key　　　　 缓存的key
     */
    void notify(String nameSpace, String key);
}
