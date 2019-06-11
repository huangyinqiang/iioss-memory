package net.iioss.memory.core.impl.redis;

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import net.iioss.memory.core.definition.CommonMemory;
import net.iioss.memory.core.exception.MemoryException;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.impl.lettuce
 * @Description: redis
 * @date 2019/6/9 23:06
 */
public abstract class RedisCommonMemory implements CommonMemory {

    protected String namespace;
    protected String region;
    protected GenericObjectPool<StatefulConnection<String, byte[]>> pool;

    protected StatefulConnection connect() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new MemoryException(e);
        }
    }

    protected BaseRedisCommands sync(StatefulConnection conn) {
        if(conn instanceof StatefulRedisClusterConnection)
            return ((StatefulRedisClusterConnection)conn).sync();
        else if(conn instanceof StatefulRedisConnection)
            return ((StatefulRedisConnection)conn).sync();
        return null;
    }

}
