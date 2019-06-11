package net.iioss.memory.core.constant;

import cn.hutool.core.util.ObjectUtil;

/**
 * @Title 类型
 * @auther huangyinqiang
 * @create 2019-06-05 下午7:23
 */
public enum Type {


    /**
     * 广播类型
     */
    BROADCAST("broadcast"),


    /**
     * 进程内存类型
     */
    PROCESS_MEMORY("processMemory"),

    /**
     * 进程外内存类型
     */
    COMMON_MEMORY("commonMemory"),

    /**
     * 序列化类型
     */
    SERIALIZER("serializer"),

    NONE("none");

    private final String name;

    Type(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public static Type getTypeByName(String name){
        for(Type type:Type.values()){
            if(name.equals(type.getName())){
                return type;
            }
        }
        return  null;
    }
    public static boolean existName(String name){
        return !ObjectUtil.isNull(getTypeByName(name));
    }
}