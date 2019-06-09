package net.iioss.memory.core.util;

import cn.hutool.core.util.ObjectUtil;

import java.util.Properties;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.util
 * @Description: Properties工具类
 * @date 2019/6/6 4:42
 */
public class PropertiesUtil {

    public static Properties getSubProperties(String prefix,Properties properties) {
        Properties pro = new Properties();
        final String pre = prefix + '.';
        properties.forEach((k, v) -> {
            String key = (String) k;
            if (key.startsWith(pre)) {
                String value = (String) v;
                if (ObjectUtil.isNotNull(value))
                    value = value.trim();
                pro.setProperty(key.substring(pre.length()), value);
            }
        });
        return pro;
    }

}
