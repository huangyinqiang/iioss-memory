package net.iioss.memory.core;

import cn.hutool.core.lang.Singleton;
import net.iioss.memory.core.definition.CommonMemory;
import net.iioss.memory.core.cluster.Cluster;
import net.iioss.memory.core.cluster.ClusterFactory;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.constant.Type;
import net.iioss.memory.core.serializer.SerializationAdapter;
import net.iioss.memory.core.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory
 * @Description: TODO
 * @date 2019/6/6 6:02
 */
public class MemoryBuilder {

    private final static Logger log = LoggerFactory.getLogger(MemoryBuilder.class);

    private MemoryChannel channel;
    private MemoryAdmin admin;
    private Cluster policy; //不同的广播策略
    private AtomicBoolean opened = new AtomicBoolean(false);
    private Config config;

    private MemoryBuilder(Config config) {
        this.config = config;
    }

    public final static MemoryBuilder init(Config config) {
        return new MemoryBuilder(config);
    }

    /**
     * 返回缓存操作接口
     *
     * @return CacheChannel
     */
    public MemoryChannel getChannel() {
        if (this.channel == null || !this.opened.get()) {
            synchronized (MemoryBuilder.class) {
                if (this.channel == null || !this.opened.get()) {
                    this.initFromConfig(config);
                    /* 初始化缓存接口 */
                    this.channel = new MemoryChannel() {
                        @Override
                        public void sendClearCmd(String region) {
                            policy.sendClearCmd(region);
                        }

                        @Override
                        public void senddeleteCmd(String region, String... keys) {
                            policy.sendDeleteCmd(region, keys);
                        }

                        @Override
                        public void close() {
                            super.close();
                            policy.disconnect();
                            admin.shutdown();
                            opened.set(false);
                        }
                    };
                    this.opened.set(true);
                }
            }
        }
        return this.channel;
    }

    /**
     */
    public void close() {
        this.channel.close();
        this.channel = null;
    }

    /**
     * 加载配置
     *
     * @return
     */
    private void initFromConfig(Config config) {
        SerializationAdapter.init(config.getFileConfig().get(Type.SERIALIZER).getName());
        //初始化两级的缓存管理
        this.admin = MemoryAdmin.init((region, key) -> {
            //当一级缓存中的对象失效时，自动清除二级缓存中的数据
            CommonMemory level2 = this.admin.getCommonMemory(region);
            level2.delete(key);
            this.admin.getProcessMemory(region).delete(key);
            log.debug("Level 1 cache object expired, evict level 2 cache object [{},{}]", region, key);
            if (policy != null)
                policy.sendDeleteCmd(region, key);
        });
        Properties subProperties = PropertiesUtil.getSubProperties(config.getFileConfig().get(Type.BROADCAST).getName(), Singleton.get(Properties.class));

        policy = ClusterFactory.init(admin, config.getFileConfig().get(Type.BROADCAST).getName(), subProperties);
        log.info("使用集群模式 : {}", policy.getClass().getName());
    }


}
