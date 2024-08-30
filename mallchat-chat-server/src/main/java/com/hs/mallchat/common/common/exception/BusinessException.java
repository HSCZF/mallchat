package com.hs.mallchat.common.common.exception;

import lombok.Data;

/**
 * <h1>业务异常类</h1>
 * <p>
 * 该类继承自{@link RuntimeException}，用于封装业务处理过程中遇到的异常情况，
 * 包含错误码{@code errorCode}和错误信息{@code errorMsg}，以便于更精确地描述错误原因，
 * 并方便前端或调用方进行错误处理和提示。
 * </p>
 *
 * @author CZF
 * @Create:  2024/6/7 16:09
 */
@Data
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected Integer errorCode;

    protected String errorMsg;

    public BusinessException() {
        super();
    }

    /**
     * 构造方法，使用错误信息初始化异常。
     *
     * @param errorMsg 错误信息描述
     */
    public BusinessException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    /**
     * 构造方法，使用错误码和错误信息初始化异常。
     *
     * @param errorCode 错误码
     * @param errorMsg  错误信息描述
     */
    public BusinessException(Integer errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BusinessException(Integer errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 构造方法，根据错误枚举{@link ErrorEnum}初始化异常。
     *
     * @param errorEnum 包含错误码和信息的错误枚举
     */
    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getErrorMsg());
        this.errorCode = errorEnum.getErrorCode();
        this.errorMsg = errorEnum.getErrorMsg();
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}

