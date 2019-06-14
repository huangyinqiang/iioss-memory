package net.iioss.memory.core;

import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.definition.CommonMemory;
import net.iioss.memory.core.cluster.Cluster;
import net.iioss.memory.core.cluster.ClusterFactory;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.constant.Type;
import net.iioss.memory.core.serializer.SerializationAdapter;
import net.iioss.memory.core.util.SettingUtil;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory
 * @Description: 内存操作构建者
 * @date 2019/6/6 6:02
 */
@Slf4j
public class MemoryBuilder {

    private MemoryProvider channel;
    private MemoryAdmin admin;
    private Cluster policy;
    private AtomicBoolean opened = new AtomicBoolean(false);
    private Config config;

    private MemoryBuilder(Config config) {
        this.config = config;
    }

    public final static MemoryBuilder init(Config config) {
        return new MemoryBuilder(config);
    }

    /**
     * 获取频道
     * @return MemoryProvider
     */
    public MemoryProvider getChannel() {
        if (channel == null || !opened.get()) {
            synchronized (MemoryBuilder.class) {
                if (channel == null || !opened.get()) {
                    parseConfig(config);
                    channel = new MemoryProvider() {
                        @Override
                        public void sendClearCmd(String nameSpace) {
                            policy.sendClearCmd(nameSpace);
                        }

                        @Override
                        public void senddeleteCmd(String nameSpace, String... keys) {
                            policy.sendDeleteCmd(nameSpace, keys);
                        }

                        @Override
                        public void close() {
                            super.close();
                            policy.disconnect();
                            admin.shutdown();
                            opened.set(false);
                        }
                    };
                    opened.set(true);
                }
            }
        }
        return channel;
    }

    /**
     */
    public void close() {
        this.channel.close();
        this.channel = null;
    }

    /**
     * 根据加载的config进行解析
     * @param config　配置文件
     */
    private void parseConfig(Config config) {
        //配置序列化方案
        SerializationAdapter.init(config.getFileConfig().get(Type.SERIALIZER).getName());
        //配置内存的管理器
        admin = MemoryAdmin.init((nameSpace, key) -> {
            CommonMemory common =admin.getCommonMemory(nameSpace);
            common.delete(key);
            if (!common.isSupportTimeToLiveSeconds()) {
                admin.getProcessMemory(nameSpace).delete(key);
            }
            this.admin.getProcessMemory(nameSpace).delete(key);
            log.debug("进程数据过期, 自动清除内存数据 [{},{}]", nameSpace, key);
            if (policy != null)
                policy.sendDeleteCmd(nameSpace, key);
        });
        policy = ClusterFactory.init(admin, config.getFileConfig().get(Type.BROADCAST).getName(),
                SettingUtil.getMapByPrefix(config.getFileConfig().get(Type.BROADCAST).getName()));
        log.info("使用集群模式:{}", policy.getClass().getName());
    }


}
