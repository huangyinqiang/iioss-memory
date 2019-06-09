package net.iioss.memory.core.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @Title 内存操作异常类
 * @auther huangyinqiang
 * @create 2019-06-05 下午3:00
 */
public class MemoryException extends RuntimeException {

    private static final long serialVersionUID = -7442601300619030550L;

    public MemoryException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public MemoryException(String message) {
        super(message);
    }

    public MemoryException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public MemoryException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MemoryException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

}
