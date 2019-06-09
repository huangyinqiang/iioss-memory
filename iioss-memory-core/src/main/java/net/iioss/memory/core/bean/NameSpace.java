package net.iioss.memory.core.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.entry
 * @Description: 名称空间
 * @date 2019/6/6 3:57
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class NameSpace {

    private String name;
    private long size;
    private long ttl;
}
