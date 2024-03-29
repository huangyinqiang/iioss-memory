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
 *
 * redis清空缓存指令：
 * １.del iioss-cache:你的空间名称
 * 2. publish iioss-memory '{"keys":[],"operator":3,"region":"Users"}'
 *
 */
@Slf4j
public class Memory {

    /*缓存提供者*/
    private static MemoryProvider provider;
    /*默认的名称空间*/
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
     * 删除　内存数据
     * @param keys　主键
     */
    public static void del(String ... keys)  {
        getChannel().delete(nameSpace,keys);
    }

    /**
     * 删除数据
     * @param nameSpace　名称空间
     * @param keys　　　主键
     */
    public static void del(String nameSpace,String ... keys)  {
        getChannel().delete(nameSpace,keys);
    }


    /**
     * 清空缓存
     * @param nameSpace　名称空间
     */
    public static void clear(String nameSpace)  {
        getChannel().clear(nameSpace);
    }

    /**
     * 清空默认缓存
     */
    public static void clear()  {
        getChannel().clear(nameSpace);
    }





    /**
     * 获取当前内存操作的频道
     * @return 当前操作频道
     */
    public static MemoryProvider getChannel(){
        return ObjectUtil.isNull(provider)?
                (provider=MemoryBuilder.init(Config.loadConfig()).getChannel()):provider;
    }


    /**
     * 获取当前内存操作的频道
     * @return 当前操作频道
     */
    public static MemoryProvider getChannel(String fileName){
        return ObjectUtil.isNull(provider)?
                (provider=MemoryBuilder.init(Config.loadConfig(fileName)).getChannel()):provider;
    }


    /**
     * 关闭内存操作类
     */
    public static void close() {
        if (ObjectUtil.isNull(provider))
            throw new MemoryException("尚未建立内存频道");
        provider.close();
    }

















    public static void main(String[] args) {
        log.info("已经开始启动");
        String namespace = "Users";


        Memory.put(namespace, "ed","8888");
        Memory.put(namespace, "ed","8899999999");

        //Memory.clear(namespace);





        for (;;){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            MemoryObject fff2 = Memory.get(namespace, "ed");

            System.out.println("fff2       "+fff2);
        }



    }


}
