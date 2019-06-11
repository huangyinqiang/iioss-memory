package net.iioss.memory.core.definition;

/**
 * @author HuangYinQiang
 * @version 1.0
 * @Package net.iioss.memory.core.base
 * @Description: 进程内存操作基类
 * @date 2019/6/9 13:31
 */
public interface ProcessMemory extends Memory {


    /**
     * 内存最大元素个数：MaxEntriesLocalHeap
     * @return 内存最大元素个数
     */
    long getMaxSize();
}
