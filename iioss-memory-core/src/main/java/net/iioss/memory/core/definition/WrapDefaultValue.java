package net.iioss.memory.core.definition;

import net.iioss.memory.core.bean.ConfigEntry;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.base
 * @Description: Wrap的默认值
 * @date 2019/6/9 14:24
 */
public interface WrapDefaultValue {

    /**
     * 默认值
     */
    ConfigEntry defaultValue(ConfigEntry wrap);
}
