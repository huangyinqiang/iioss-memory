package net.iioss.memory.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import net.iioss.memory.core.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static net.iioss.memory.core.constant.NameDefinition.DELIMITER;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.util
 * @Description: Setting工具类
 * @date 2019/6/6 4:42
 */
@Slf4j
public class SettingUtil extends cn.hutool.setting.SettingUtil{


    public static Setting get(String name) {
        try {
            return cn.hutool.setting.SettingUtil.get(name);
        }catch (Exception e){
            log.warn("没有配置核心配置文件，按照默认值方案既定运行！");
            return new Setting();
        }
    }

    /**
     * 根据前缀获取Setting里面的信息
     * @param prefix　　前缀
     * @return 关于前缀相关的配置
     */
    public static Map<String, String> getMapByPrefix(String prefix){
        return getMapByPrefix(prefix,Singleton.get(Setting.class));
    }


    /**
     * 根据前缀获取Setting里面的信息
     * @param prefix　　前缀
     * @param setting　　文件
     * @return 关于前缀相关的配置
     */
    public static Map<String, String> getMapByPrefix(String prefix,Setting setting){
        Map<String, String> resultMap = CollectionUtil.newHashMap();
        Map<String, String> configMap = CollectionUtil.toMap(setting.entrySet());
        CollectionUtil.forEach(configMap, (e,f,i)->{
            if (e.startsWith(prefix+DELIMITER)){
                resultMap.put(e.substring((prefix+DELIMITER).length()), f);
            }
        });
        return resultMap;
    }



}
