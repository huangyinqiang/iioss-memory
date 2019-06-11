package net.iioss.memory.core.cluster;


import net.iioss.memory.core.MemoryAdmin;
import net.iioss.memory.core.constant.Command;

import java.util.Properties;

/**
 * 实现空的集群通知策略
 * @author Winter Lau(javayou@gmail.com)
 */
public class NoneCluster implements Cluster {

    private int LOCAL_COMMAND_ID = Command.genRandomSrc();

    @Override
    public void connect(Properties props, MemoryAdmin admin) {

    }

    @Override
    public void publish(Command cmd) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void delete(String nameSpace, String... keys) {

    }

    @Override
    public void clear(String nameSpace) {

    }

    @Override
    public boolean isSelfCommand(Command cmd) {
        return cmd.getSrc() == LOCAL_COMMAND_ID;
    }
}
