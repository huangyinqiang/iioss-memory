package net.iioss.memory.core.definition;
/**
 * @Title 类型
 * @auther huangyinqiang
 * @create 2019-06-05 下午7:23
 */
public enum Type {


    /**
     * 广播类型
     */
    BROADCAST,


    /**
     * 进程内存类型
     */
    PROCESS_MEMORY,

    /**
     * 进程外内存类型
     */
    COMMON_MEMORY,

    /**
     * 序列化类型
     */
    SERIALIZER,

    /**
     * 未知的类型
     */
    NONE;
}