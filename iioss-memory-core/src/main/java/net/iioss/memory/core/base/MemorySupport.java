package net.iioss.memory.core.base;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.setting.Setting;
import net.iioss.memory.core.bean.NameSpace;
import net.iioss.memory.core.definition.MemoryLevel;

import java.util.Collection;
import java.util.Properties;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.base
 * @Description: 内存操作类的基类
 * @date 2019/6/9 14:26
 */
public interface MemorySupport {

    /**
     * Memory标识
     * @return
     */
    String getName();

    /**
     * Memory的级别
     * @return
     */
    MemoryLevel getLevel();

    /**
     * 判断Memory的层级
     * @param level 层级数
     * @return
     */
    default boolean isLevel(MemoryLevel level) {
        return !ObjectUtil.isNull(level) && getLevel() == level;
    }

    /**
     * 构建内Memory
     * @param nameSpace  名称空间
     * @param listener   监听器
     * @return  Memory
     */
    Memory build(String nameSpace, ProcessMemoryListener listener);

    /**
     * 带过期时间的构建
     * @param  nameSpace  名称空间
     * @param timeToLiveSeconds  过期事件
     * @param listener    监听器
     * @return
     */
    Memory build(String nameSpace, long timeToLiveSeconds, ProcessMemoryListener listener);

    /**
     * 移出Memory该名称空间下的所有数据
     * @param nameSpace 名称空间
     */
    default void removeCache(String nameSpace) {}

    /**
     * 获取当前memory所有名称空间
     * @return
     */
    Collection<NameSpace> getNameSpaces();

    /**
     * 开启当前的memory
     * @param props 配置的setting
     */
    void start(Properties props);


    /**
     * 停止当前memory
     */
    void stop();

}
