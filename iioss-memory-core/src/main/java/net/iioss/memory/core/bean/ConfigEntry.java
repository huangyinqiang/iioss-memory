package net.iioss.memory.core.bean;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import lombok.Data;
import lombok.experimental.Accessors;
import net.iioss.memory.core.definition.WrapDefaultValue;
import net.iioss.memory.core.constant.*;
import net.iioss.memory.core.exception.MemoryException;
import net.iioss.memory.core.util.SettingUtil;

import java.util.Map;

import static net.iioss.memory.core.constant.NameDefinition.*;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.bean
 * @Description: 包装bean基类
 * @date 2019/6/9 18:36
 */
@Data
@Accessors(chain = true)
public class ConfigEntry {
    public static final String FILE_BASE=BASE_PRE+DELIMITER+MEMORY+DELIMITER;

    /**
     * 标志
     */
    private String name;

    /**
     * 类型
     */
    private Type type;

    /**
     * 内存
     */
    private MemoryType memoryType;

    /**
     * 广播
     */
    private BroadcastType broadcastType;

    /**
     * 序列化
     */
    private SerializerType serializerType;


    /**
     * 内存级别
     */
    private MemoryLevel memoryLevel;

    /**
     * 更多的配置
     */
    private Map<String, String> manyConfig;

    /**
     * 配置文件的key
     * @return 配置文件的key
     */
    public String getKey(){
        return FILE_BASE+type.getName();
    }


    /**
     * 根据配置装配
     */
    public ConfigEntry configOfWrap() {
        Setting setting =  Singleton.get(Setting.class);
        String property = setting.get(getKey());
        manyConfig= SettingUtil.getMapByPrefix(getKey());
        if (StrUtil.isNotEmpty(property)){
            property = StrUtil.trim(property);
            if (type==Type.BROADCAST){
                BroadcastType type = BroadcastType.getBroadcastTypeByName(property);
                if (ObjectUtil.isNotNull(type)){
                    this.setBroadcastType(type).setName(type.getName());
                }else {
                    this.setBroadcastType(null).setName(property);
                }
            }else if (type==Type.PROCESS_MEMORY){
                MemoryType type = MemoryType.getMemoryTypeByName(property);
                if (ObjectUtil.isNotNull(type)){
                    this.setMemoryType(type).setName(type.getName()).setMemoryLevel(MemoryLevel.ONE);
                }else {
                    this.setMemoryType(null).setName(property);
                }
            }else if (type==Type.COMMON_MEMORY){
                MemoryType type = MemoryType.getMemoryTypeByName(property);
                if (ObjectUtil.isNotNull(type)){
                    this.setMemoryType(type).setName(type.getName()).setMemoryLevel(MemoryLevel.TWO);
                }else {
                    this.setMemoryType(null).setName(property);
                }
            }else if (type==Type.SERIALIZER){
                SerializerType serializerTypeByName = SerializerType.getSerializerTypeByName(property);
                if (ObjectUtil.isNotNull(serializerTypeByName)){
                    this.setSerializerType(serializerTypeByName).setName(serializerTypeByName.getName());
                }else {
                    this.setSerializerType(null).setName(property);
                }
            }else {
                throw new MemoryException("未知类型");
            }
        }
        return this;
    }


    /**
     * 获取用户最终方案
     * @return 最终方案
     */
    public static ConfigEntry init(ConfigEntry wrap, WrapDefaultValue defaultValue){
        return defaultValue.defaultValue(wrap).configOfWrap();
    }

}
