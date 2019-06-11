package net.iioss.memory.core.constant;

import cn.hutool.core.util.ObjectUtil;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.definition
 * @Description: 序列化方案
 * @date 2019/6/9 13:41
 */
public enum SerializerType {

    /**
    * fst序列化方案
    * */
    FST("fst","net.iioss.memory.core.serializer.FSTSerializer"),
    /**
     * fastjson序列化方案
     */
    FASTJSON("fastjson","net.iioss.memory.core.serializer.FastjsonSerializer"),
    /**
     * java原生的序列化方案
     */
    JAVA("java","net.iioss.memory.core.serializer.JavaSerializer");



    private final String name;
    private final String className;
    SerializerType(String name, String className) {
        this.name=name;
        this.className=className;
    }
    public String getName() {
        return name;
    }
    public String getClassName() {
        return className;
    }
    public static SerializerType getSerializerTypeByName(String name){
        for(SerializerType type:SerializerType.values()){
            if(name.equals(type.getName())){
                return type;
            }
        }
        return  null;
    }
    public static SerializerType getSerializerTypeByClassName(String className){
        for(SerializerType type:SerializerType.values()){
            if(className.equals(type.getClassName())){
                return type;
            }
        }
        return  null;
    }
    public static boolean existName(String name){
        return !ObjectUtil.isNull(getSerializerTypeByName(name));
    }

}
