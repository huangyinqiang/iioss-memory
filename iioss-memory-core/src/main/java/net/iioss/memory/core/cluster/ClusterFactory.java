package net.iioss.memory.core.cluster;

import net.iioss.memory.core.MemoryAdmin;
import net.iioss.memory.core.impl.lettuce.LettuceCacheProvider;
import net.sf.ehcache.CacheException;

import java.util.Properties;

/**
 * @Title 集群方式工厂
 * @auther huangyinqiang
 * @create 2019-06-06 上午11:43
 */
public class ClusterFactory {

    public final static Cluster init(MemoryAdmin holder, String broadcast, Properties props) {
        Cluster cluster;
        if ("lettuce".equalsIgnoreCase(broadcast))
            cluster = ClusterFactory.lettuce(props, holder);
        else if ("none".equalsIgnoreCase(broadcast))
            cluster = new NoneCluster();
        else
            cluster = ClusterFactory.custom(broadcast, props, holder);
        return cluster;
    }


    private final static Cluster lettuce(Properties props, MemoryAdmin holder) {
        LettuceCacheProvider policy = new LettuceCacheProvider();
        policy.connect(props, holder);
        return policy;
    }

    private final static Cluster custom(String classname, Properties props, MemoryAdmin holder) {
        try {
            Cluster policy = (Cluster)Class.forName(classname).newInstance();
            policy.connect(props, holder);
            return policy;
        } catch (Exception e) {
            throw new CacheException("Failed in load custom cluster policy. class = " + classname, e);
        }
    }
}
