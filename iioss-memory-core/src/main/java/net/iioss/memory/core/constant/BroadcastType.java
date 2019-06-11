package net.iioss.memory.core.constant;

import cn.hutool.core.util.ObjectUtil;

/**
 * @Title 广播方式定义
 * @auther huangyinqiang
 * @create 2019-06-05 下午7:23
 */
public enum BroadcastType {

    REDIS("redis",""),

    CUSTOMER("customer",""),

    NONE("none","");


    private final String name;
    private final String className;

    BroadcastType(String name, String className) {
        this.name=name;
        this.className=className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public static BroadcastType getBroadcastTypeByName(String chineseName){
        for(BroadcastType type:BroadcastType.values()){
            if(chineseName.equals(type.getName())){
                return type;
            }
        }
        return  null;
    }

    public static boolean existName(String name){
        return !ObjectUtil.isNull(getBroadcastTypeByName(name));
    }

}