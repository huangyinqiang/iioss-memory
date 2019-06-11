package net.iioss.memory.core.impl;

import net.iioss.memory.core.definition.Memory;
import net.iioss.memory.core.definition.MemorySupport;
import net.iioss.memory.core.definition.ProcessMemoryListener;
import net.iioss.memory.core.bean.NameSpace;
import net.iioss.memory.core.constant.MemoryLevel;
import net.iioss.memory.core.constant.MemoryType;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.impl
 * @Description: TODO
 * @date 2019/6/9 23:23
 */
public class NullCacheSupport  implements MemorySupport {
    @Override
    public String getName() {
        return MemoryType.NONE.getName();
    }

    @Override
    public MemoryLevel getLevel() {
        return null;
    }

    @Override
    public Memory build(String nameSpace, ProcessMemoryListener listener) {
        return null;
    }

    @Override
    public Memory build(String nameSpace, long timeToLiveSeconds, ProcessMemoryListener listener) {
        return null;
    }

    @Override
    public Collection<NameSpace> getNameSpaces() {
        return null;
    }

    @Override
    public void start(Map<String, String> configMap) {

    }

    @Override
    public void stop() {

    }
}
