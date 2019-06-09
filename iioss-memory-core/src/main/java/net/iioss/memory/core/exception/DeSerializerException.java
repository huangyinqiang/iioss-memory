package net.iioss.memory.core.exception;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @Title 反序列化的异常类
 * @auther huangyinqiang
 * @create 2019-06-05 下午3:00
 */
public class DeSerializerException extends RuntimeException {

    private static final long serialVersionUID = -2698343464063906959L;

    public DeSerializerException(Throwable e) {
        super(ExceptionUtil.getMessage(e), e);
    }

    public DeSerializerException(String message) {
        super(message);
    }

    public DeSerializerException(String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params));
    }

    public DeSerializerException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DeSerializerException(Throwable throwable, String messageTemplate, Object... params) {
        super(StrUtil.format(messageTemplate, params), throwable);
    }

}
