package net.iioss.memory.core.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @Title 序列化的异常类
 * @auther huangyinqiang
 * @create 2019-06-05 下午3:00
 */
public class SerializerException extends RuntimeException {

    private static final long serialVersionUID = -2698483464063906959L;

    public SerializerException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public SerializerException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SerializerException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

}
