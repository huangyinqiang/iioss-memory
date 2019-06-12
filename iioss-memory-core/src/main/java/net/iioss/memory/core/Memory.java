package net.iioss.memory.core;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.bean.MemoryObject;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.exception.MemoryException;
import static net.iioss.memory.core.constant.NameDefinition.DEFAULT_NAMESPACE;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core
 * @Description: 起始类，供外部使用
 * @date 2019/6/9 13:25
 */
@Slf4j
public class Memory {

    /**
     * 当前内存的操作频道
     */
    private static MemoryChannel builder;

    /**
     * 默认的名称空间
     */
    private static final String nameSpace=DEFAULT_NAMESPACE;


    /**
     * 获取缓存数据
     * @param nameSpace　名称空间
     * @param key　　　　　　key
     * @return           内存数据
     */
    public static MemoryObject get(String nameSpace,String key)  {
        return getChannel().get(nameSpace, key);
    }


    /**
     * 获取缓存数据
     * @param key　　　　　　key
     * @return           内存数据
     */
    public static MemoryObject get(String key)  {
        return getChannel().get(nameSpace, key);
    }


    /**
     * 存储缓存数据
     * @param nameSpace　名称空间
     * @param key　　　　　　key
     * @return           内存数据
     */
    public static void put(String nameSpace, String key,Object object)  {
        getChannel().put(nameSpace, key,object);
    }

    /**
     * 存储缓存数据
     * @param key　　　　　　key
     * @return           内存数据
     */
    public static void put(String key,Object object)  {
        getChannel().put(nameSpace, key,object);
    }


    /**
     * 获取当前内存操作的频道
     * @return 当前操作频道
     */
    public static MemoryChannel getChannel(){
        return ObjectUtil.isNull(builder)?
                (builder=MemoryBuilder.init(Config.loadConfig()).getChannel()):builder;
    }


    /**
     * 获取当前内存操作的频道
     * @return 当前操作频道
     */
    public static MemoryChannel getChannel(String fileName){
        return ObjectUtil.isNull(builder)?
                (builder=MemoryBuilder.init(Config.loadConfig(fileName)).getChannel()):builder;
    }


    /**
     * 关闭内存操作类
     */
    public static void close() {
        if (ObjectUtil.isNull(builder))
            throw new MemoryException("尚未建立内存频道");
        builder.close();
    }


    public static void main(String[] args) {
        log.info("已经开始启动");
        String namespace = "Users";

        MemoryObject ddd = Memory.get(namespace, "ed");
        System.out.println("ddd       "+ddd);
        Memory.put(namespace, "ed","44444444444444444444444444");
        MemoryObject fff = Memory.get(namespace, "ed");

        System.out.println("fff       "+fff);



        for (;;){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MemoryObject ddd2 = Memory.get(namespace, "ed");
            System.out.println("ddd2       "+ddd2);
            MemoryObject fff2 = Memory.get(namespace, "ed");

            System.out.println("fff2       "+fff2);
        }



    }


}