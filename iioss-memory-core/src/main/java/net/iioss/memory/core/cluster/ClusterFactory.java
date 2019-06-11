package net.iioss.memory.core.cluster;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Singleton;
import net.iioss.memory.core.MemoryAdmin;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.constant.BroadcastType;
import net.iioss.memory.core.constant.Type;
import net.iioss.memory.core.impl.redis.RedisCommonMemorySupport;
import net.iioss.memory.core.util.SettingUtil;
import net.sf.ehcache.CacheException;

import java.util.Map;

/**
 * @Title 集群方式工厂
 * @auther huangyinqiang
 * @create 2019-06-06 上午11:43
 */
public class ClusterFactory {

    public final static Cluster init(MemoryAdmin holder, String broadcast, Map<String, String> configMap) {
        Cluster cluster;
        if (BroadcastType.REDIS.getName().equalsIgnoreCase(broadcast))
            cluster = ClusterFactory.redis(holder);
        else if (BroadcastType.NONE.getName().equalsIgnoreCase(broadcast))
            cluster = new NoneCluster();
        else
            cluster = ClusterFactory.custom(broadcast, holder,configMap);
        return cluster;
    }


    private final static Cluster redis(MemoryAdmin holder) {
        Map<String, String> redisConfig = SettingUtil.getMapByPrefix(Singleton.get(Config.class).getFileConfig().get(Type.COMMON_MEMORY).getKey());
        RedisCommonMemorySupport policy = new RedisCommonMemorySupport();
        policy.connect(redisConfig, holder);
        return policy;
    }

    private final static Cluster custom(String classname, MemoryAdmin holder, Map<String, String> configMap) {
        try {
            Cluster policy = (Cluster)Class.forName(classname).newInstance();
            policy.connect(CollectionUtil.newHashMap(), holder);
            return policy;
        } catch (Exception e) {
            throw new CacheException("Failed in load custom cluster policy. class = " + classname, e);
        }
    }
}
