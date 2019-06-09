package net.iioss.memory.core.cluster;

import cn.hutool.core.bean.BeanUtil;
import net.iioss.memory.core.MemoryAdmin;
import net.iioss.memory.core.definition.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static net.iioss.memory.core.definition.NameDefinition.*;

/**
 * @Title 集群基类
 * @auther huangyinqiang
 * @create 2019-06-05 上午10:20
 */
public interface Cluster{
    Logger log = LoggerFactory.getLogger(Cluster.class);

    /**
     * 链接集群
     * @param props　集群配置属性
     * @param admin　内存管理器
     */
    void connect(Properties props, MemoryAdmin admin);


    /**
     * 发布指令
     * @param cmd　指令
     */
    void publish(Command cmd);


    /**
     * 断开集群
     */
    void disconnect();


    /**
     * 删除指定的keys
     * @param nameSpace　名称空间
     * @param keys　　　　指定的keys
     */
    void delete(String nameSpace, String... keys);


    /**
     * 清空整个名称空间的所有内容
     * @param nameSpace　名称空间
     */
    void clear(String nameSpace);


    /**
     * 判断是否是自身进程命令
     * @param cmd 是否是自身进程命令
     * @return 布尔值
     */
    boolean isSelfCommand(Command cmd);


    /**
     * 发布删除命令
     * @param nameSpace　命令空间
     * @param keys　　　　keys
     */
    default void sendDeleteCmd(String nameSpace, String... keys) {
        publish(new Command(Command.OPT_EVICT_KEY, nameSpace, keys));
    }


    /**
     * 发布清空命令
     * @param nameSpace　名称空间
     */
    default void sendClearCmd(String nameSpace) {
        publish(new Command(Command.OPT_CLEAR_KEY, nameSpace));
    }


    /**
     * 处理当前接收到的指令
     * @param cmd　指令
     */
    default void handleCommand(Command cmd) {
        try {
            if (cmd == null || isSelfCommand(cmd))
                return;
            switch (cmd.getOperator()) {
                case Command.OPT_JOIN:
                    log.info("加入节点  -> {}", cmd.getSrc());
                    break;
                case Command.OPT_EVICT_KEY:
                    this.delete(cmd.getRegion(), cmd.getKeys());
                    log.debug("删除指令, nameSpace={}  key={}",cmd.getRegion(),String.join(DELIMITER, cmd.getKeys()));
                    break;
                case Command.OPT_CLEAR_KEY:
                    this.clear(cmd.getRegion());
                    log.debug("清空指令, nameSpace={}",cmd.getRegion());
                    break;
                case Command.OPT_QUIT:
                    log.info("退出节点  -> {}", cmd.getSrc());
                    break;
                default:
                    log.warn("未知命令 = " + cmd.getOperator());
            }
        } catch (Exception e) {
            log.error("处理指令发生错误　cmd={} ",BeanUtil.beanToMap(cmd));
        }
    }
}
