/**
 * Copyright (c) 2015-2017, Winter Lau (javayou@gmail.com), wendal.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.iioss.memory.core.impl.redis;

import cn.hutool.core.util.StrUtil;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import net.iioss.memory.core.MemoryAdmin;
import net.iioss.memory.core.constant.MemoryType;
import net.iioss.memory.core.definition.CommonMemory;
import net.iioss.memory.core.definition.Memory;
import net.iioss.memory.core.definition.MemorySupport;
import net.iioss.memory.core.definition.ProcessMemoryListener;
import net.iioss.memory.core.bean.NameSpace;
import net.iioss.memory.core.cluster.Cluster;
import net.iioss.memory.core.constant.Command;
import net.iioss.memory.core.constant.MemoryLevel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.iioss.memory.core.constant.NameDefinition.PROJECT_NAME;

/**
 *  redis支撑
 */
public class RedisCommonMemorySupport extends RedisPubSubAdapter<String, String> implements MemorySupport, Cluster {

    private int LOCAL_COMMAND_ID = Command.genRandomSrc();
    private static final RedisCustomerCodec codec = new RedisCustomerCodec();
    private static AbstractRedisClient redisClient;
    GenericObjectPool<StatefulConnection<String, byte[]>> pool;
    private StatefulRedisPubSubConnection<String, String> pubsub_subscriber;
    private String storage;
    private MemoryAdmin holder;
    private String channel;
    private String namespace;
    private final ConcurrentHashMap<String, CommonMemory> regions = new ConcurrentHashMap();

    @Override
    public String getName() {
        return MemoryType.REDIS.getName();
    }

    @Override
    public MemoryLevel getLevel() {
        return MemoryLevel.TWO;
    }

    @Override
    public boolean isSelfCommand(Command cmd) {
        return cmd.getSrc() == LOCAL_COMMAND_ID;
    }

    @Override
    public void start(Map<String, String> configMap) {
        this.namespace = configMap.get("namespace");
        this.storage = StrUtil.emptyToDefault(configMap.get("storage"),"hash");
        this.channel = StrUtil.emptyToDefault(configMap.get("channel"),PROJECT_NAME);

        //redis集群方式
        String scheme = StrUtil.emptyToDefault(configMap.get("scheme"), "redis");
        String hosts = StrUtil.emptyToDefault(configMap.get("hosts"),"127.0.0.1:6379");
        String password =StrUtil.emptyToDefault(configMap.get("password"), "iioss99!");
        int database = Integer.parseInt(StrUtil.emptyToDefault(configMap.get("database"), "0"));
        String sentinelMasterId = configMap.get("sentinelMasterId");

        boolean isCluster = false;
        if("redis-cluster".equalsIgnoreCase(scheme)) {
            scheme = "redis";
            isCluster = true;
        }

        String redis_url = String.format("%s://%s@%s/%d#%s", scheme, password, hosts, database, sentinelMasterId);

        redisClient = isCluster?RedisClusterClient.create(redis_url):RedisClient.create(redis_url);
        try {
            int timeout = Integer.parseInt(StrUtil.emptyToDefault(configMap.get("timeout"), "10000"));
            redisClient.setDefaultTimeout(Duration.ofMillis(timeout));
        }catch(Exception e){
            log.warn("Failed to set default timeout, using default 10000 milliseconds.", e);
        }

        //connection pool configurations
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(Integer.parseInt(StrUtil.emptyToDefault(configMap.get("maxTotal"), "100")));
        poolConfig.setMaxIdle(Integer.parseInt(StrUtil.emptyToDefault(configMap.get("maxIdle"), "10")));
        poolConfig.setMinIdle(Integer.parseInt(StrUtil.emptyToDefault(configMap.get("minIdle"), "10")));

        pool = ConnectionPoolSupport.createGenericObjectPool(() -> {
            if(redisClient instanceof RedisClient)
                return ((RedisClient)redisClient).connect(codec);
            else if(redisClient instanceof RedisClusterClient)
                return ((RedisClusterClient)redisClient).connect(codec);
            return null;
        }, poolConfig);
    }

    @Override
    public void stop() {
        pool.close();
        regions.clear();
        redisClient.shutdown();
    }

    @Override
    public Memory build(String namespace, ProcessMemoryListener listener) {
        return regions.computeIfAbsent(this.namespace + ":" + namespace, v -> "hash".equalsIgnoreCase(this.storage)?
                new RedisCommonHashMemory(this.namespace, namespace, pool):
                new RedisCommonGenericMemory(this.namespace, namespace, pool));
    }

    @Override
    public Memory build(String namespace, long timeToLiveInSeconds, ProcessMemoryListener listener) {
        return build(namespace, listener);
    }

    @Override
    public Collection<NameSpace> getNameSpaces() {
        return Collections.emptyList();
    }

    /**
     * 删除本地某个缓存条目
     * @param region 区域名称
     * @param keys   缓存键值
     */
    @Override
    public void delete(String region, String... keys) {
        holder.getProcessMemory(region).delete(keys);
    }

    /**
     * 清除本地整个缓存区域
     * @param region 区域名称
     */
    @Override
    public void clear(String region) {
        holder.getProcessMemory(region).clear();
    }

    /**
     * Get PubSub connection
     * @return connection instance
     */
    private StatefulRedisPubSubConnection pubsub() {
        if(redisClient instanceof RedisClient)
            return ((RedisClient)redisClient).connectPubSub();
        else if(redisClient instanceof RedisClusterClient)
            return ((RedisClusterClient)redisClient).connectPubSub();
        return null;
    }

    @Override
    public void connect(Map<String, String> configMap, MemoryAdmin holder) {
        long ct = System.currentTimeMillis();
        this.holder = holder;
        this.channel = StrUtil.emptyToDefault(configMap.get("channel"),PROJECT_NAME);
        this.publish(Command.join());

        this.pubsub_subscriber = this.pubsub();
        this.pubsub_subscriber.addListener(this);
        RedisPubSubAsyncCommands<String, String> async = this.pubsub_subscriber.async();
        async.subscribe(this.channel);

        log.info("链接到redis频道:{}, time {}ms.", this.channel, System.currentTimeMillis()-ct);
    }

    @Override
    public void message(String channel, String message) {
        Command cmd = Command.parse(message);
        handleCommand(cmd);
    }

    @Override
    public void publish(Command cmd) {
    	cmd.setSrc(LOCAL_COMMAND_ID);
        try (StatefulRedisPubSubConnection<String, String> connection = this.pubsub()){
            RedisPubSubCommands<String, String> sync = connection.sync();
            sync.publish(this.channel, cmd.json());
        }
    }

    @Override
    public void disconnect() {
        try {
            this.publish(Command.quit());
            super.unsubscribed(this.channel, 1);
        } finally {
            this.pubsub_subscriber.close();
        }
    }
}
