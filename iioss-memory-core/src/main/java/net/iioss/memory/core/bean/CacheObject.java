package net.iioss.memory.core.bean;

import lombok.*;
import lombok.experimental.Accessors;
import net.iioss.memory.core.constant.MemoryLevel;

/**
 * @Title 缓存对象
 * @auther huangyinqiang
 * @create 2019-06-05 上午11:05
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Setter
@Accessors(chain = true)
public class CacheObject {

    /**
     * 缓存的名称空间
     */
    @NonNull
    @Getter
    private String nameSpace;

    /**
     * 缓存的key
     */
    @NonNull
    @Getter
    private String key;

    /**
     * 缓存的级别
     */
    @NonNull
    @Getter
    private MemoryLevel level;

    /**
     * 缓存的值
     */
    private Object value;


    public Object getValue() {
        if (value == null || value.getClass().equals(NullValue.class) || value.getClass().equals(Object.class))
            return null;
        return value;
    }

    public Object getRealValue() {
        return value;
    }

}
