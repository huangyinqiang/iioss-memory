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
@Data
@Accessors(chain = true)
public class MemoryObject {

    /**
     * 内存的名称空间
     */
    @NonNull
    private String nameSpace;

    /**
     * 内存的key
     */
    @NonNull
    private String key;

    /**
     * 内存类型
     */
    @NonNull
    private Type type;

    /**
     * 内存数据
     */
    private Object value;


    /**
     * 获取包装类
     * @return
     */
    public Object getWrapValue(){
        return value instanceof NullValue?null:value;
    }


    @Override
    public String toString() {
        return String.format("[%s,%s,%s]=>%s", nameSpace, key, type.getName(), getValue());
    }
}
