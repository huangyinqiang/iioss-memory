package net.iioss.memory.core;

import cn.hutool.core.util.ObjectUtil;
import net.iioss.memory.core.bean.MemoryObject;
import net.iioss.memory.core.config.Config;
import net.iioss.memory.core.exception.MemoryException;

import java.util.Map;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core
 * @Description: 起始类，供外部使用
 * @date 2019/6/9 13:25
 */
public class IIOSSMemory {

    /**
     * 当前内存的操作频道
     */
    private static MemoryChannel builder;


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
        MemoryChannel channel = IIOSSMemory.getChannel();
        String namespace = "Users";
        channel.put(namespace, "name", "小明");

        for (;;){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MemoryObject ffff = channel.get(namespace, "name");
            System.out.println(ffff);
        }

    }


}
