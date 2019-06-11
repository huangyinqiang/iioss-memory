package net.iioss.memory.core.cluster;

import net.iioss.memory.core.MemoryAdmin;
import net.iioss.memory.core.impl.redis.RedisCommonMemorySupport;
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
        if ("lettuce".equalsIgnoreCase(broadcast))
            cluster = ClusterFactory.lettuce(configMap, holder);
        else if ("none".equalsIgnoreCase(broadcast))
            cluster = new NoneCluster();
        else
            cluster = ClusterFactory.custom(broadcast, configMap, holder);
        return cluster;
    }


    private final static Cluster lettuce( Map<String, String> configMap, MemoryAdmin holder) {
        RedisCommonMemorySupport policy = new RedisCommonMemorySupport();
        policy.connect(configMap, holder);
        return policy;
    }

    private final static Cluster custom(String classname, Map<String, String> configMap, MemoryAdmin holder) {
        try {
            Cluster policy = (Cluster)Class.forName(classname).newInstance();
            policy.connect(configMap, holder);
            return policy;
        } catch (Exception e) {
            throw new CacheException("Failed in load custom cluster policy. class = " + classname, e);
        }
    }
}
