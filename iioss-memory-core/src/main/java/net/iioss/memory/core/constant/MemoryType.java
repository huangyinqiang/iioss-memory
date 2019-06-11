package net.iioss.memory.core.constant;

import cn.hutool.core.util.ObjectUtil;

/**
 * @Title 内存定义
 * @auther huangyinqiang
 * @create 2019-06-05 下午7:23
 */
public enum MemoryType {

    /**
     * redis内存
     */
    REDIS("redis","net.iioss.memory.core.impl.lettuce.RedisCommonMemorySupport"),

    /**
     * 客户端自定义内存
     */
    CUSTOMER("customer",""),

    /**
     * 客户端自定义内存
     */
    NONE("none",""),

    /**
     * encache内存
     */
    ENCACHE("encache","net.iioss.memory.core.impl.ehcache.EhCacheSupport");

    private final String name;
    private final String className;

    MemoryType(String name, String className) {
        this.name=name;
        this.className=className;
    }


    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public static MemoryType getMemoryTypeByName(String name){
        for(MemoryType type:MemoryType.values()){
            if(name.equals(type.getName())){
                return type;
            }
        }
        return  null;
    }

    public static boolean existName(String name){
        return !ObjectUtil.isNull(getMemoryTypeByName(name));
    }

}