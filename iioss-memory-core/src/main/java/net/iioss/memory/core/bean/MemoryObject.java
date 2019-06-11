package net.iioss.memory.core.bean;

import lombok.*;
import lombok.experimental.Accessors;
import net.iioss.memory.core.constant.MemoryLevel;
import net.iioss.memory.core.constant.Type;

/**
 * @Title 内存对象
 * @auther huangyinqiang
 * @create 2019-06-05 上午11:05
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Setter
@Accessors(chain = true)
public class MemoryObject {

    /**
     * 内存的名称空间
     */
    @NonNull
    @Getter
    private String nameSpace;

    /**
     * 内存的key
     */
    @NonNull
    @Getter
    private String key;

    /**
     * 内存类型
     */
    @NonNull
    @Getter
    private Type type;

    /**
     * 内存数据
     */
    @Getter
    private Object value;



    @Override
    public String toString() {
        return String.format("[%s,%s,%s]=>%s", nameSpace, key, type.getName(), getValue());
    }
}
