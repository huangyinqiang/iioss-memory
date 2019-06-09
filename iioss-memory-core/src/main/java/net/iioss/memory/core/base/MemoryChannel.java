package net.iioss.memory.core.base;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.base
 * @Description: 内存频道
 * @date 2019/6/9 22:15
 */
public class MemoryChannel implements AutoCloseable,Closeable {

    private static final Map<String, Object> locks = new ConcurrentHashMap<>();




    @Override
    public void close() throws IOException {

    }
}
