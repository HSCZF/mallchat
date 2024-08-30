package com.hs.mallchat.common.common.exception;

import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: CZF
 * @Create: 2024/6/7 - 15:25
 * Description: 基于RESTful API的全局异常处理类。
 * 使用@RestControllerAdvice注解将该类声明为一个处理所有REST控制器中异常的全局异常处理器。
 * 它旨在提供统一的异常处理逻辑，以确保在发生异常时，客户端接收到的响应是统一格式的。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理方法参数不合法异常。
     * 当方法参数不满足验证条件时，Spring Boot会抛出MethodArgumentNotValidException异常。
     * 该异常处理器专门捕获此类异常，并将参数验证失败的信息转换为统一的ApiResult返回。
     *
     * @param e MethodArgumentNotValidException，方法参数不合法异常对象，包含具体的错误字段和错误信息。
     * @return ApiResult<?>，返回一个封装了错误信息的ApiResult对象，用于前端展示错误详情。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResult<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 初始化一个字符串构建器，用于拼接所有的错误字段和错误信息。
        StringBuilder errorMsg = new StringBuilder();
        // 遍历所有字段错误，将字段名和错误信息追加到errorMsg中。
        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> errorMsg
                        .append(fieldError.getField())
                        .append(fieldError.getDefaultMessage())
                        .append(",")
                );
        // 将errorMsg转换为字符串，为移除最后一个逗号做准备。
        String msg = errorMsg.toString();
        // 返回一个失败的ApiResult，错误信息为拼接好的字段错误信息。
        // 通过substring移除最后一个逗号，以提供更整洁的错误信息。
        log.info("validation parameters error！The reason is:{}", msg);
        return ApiResult.fail(CommonErrorEnum.PARAM_INVALID.getCode(), msg.substring(0, msg.length() - 1));
    }

    /**
     * 业务异常：处理业务逻辑异常的异常处理器。
     * 该方法专门用于处理业务逻辑层抛出的BusinessErrorException。当遇到此类异常时，会记录错误日志并返回相应的错误结果。
     * 错误结果中包含错误码和错误信息，以便前端进行错误展示或处理。
     *
     * @param e 业务逻辑异常对象，包含具体的错误码和错误信息。
     * @return 包含错误码和错误信息的ApiResult对象，表示业务操作失败。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BusinessException.class)
    public ApiResult<?> businessErrorException(BusinessException e) {
        // 记录业务异常的日志，方便问题追踪和定位。
        log.info("business exception! The reason is :{}", e.getMessage());
        // 返回业务操作失败的结果，其中包含错误码和错误信息。
        return ApiResult.fail(e.getErrorCode(), e.getErrorMsg());
    }

    /**
     * validation参数校验异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BindException.class)
    public ApiResult bindException(BindException e) {
        StringBuilder errorMsg = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(x -> errorMsg.append(x.getField()).append(x.getDefaultMessage()).append(","));
        String message = errorMsg.toString();
        log.info("validation parameters error！The reason is:{}", message);
        return ApiResult.fail(CommonErrorEnum.PARAM_INVALID.getErrorCode(), message.substring(0, message.length() - 1));
    }

    /**
     * 处理空指针异常
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = NullPointerException.class)
    public ApiResult exceptionHandler(NullPointerException e){
        log.error("null point exception！The reason is: ", e);
        return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR);
    }

    /**
     * 未知异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ApiResult<?> systemExceptionHandler(Exception e) {
        log.error("system exception！The reason is：{}", e.getMessage(), e);
        return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR);
    }

    /**
     * http请求方式不支持
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Void> handleException(HttpRequestMethodNotSupportedException e) {
        log.error(e.getMessage(), e);
        return ApiResult.fail(-1, String.format("不支持'%s'请求", e.getMethod()));
    }

    /**
     * 限流异常
     */
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ExceptionHandler(value = FrequencyControlException.class)
    public ApiResult<?> frequencyControlExceptionHandler(FrequencyControlException e) {
        log.info("frequencyControl exception！The reason is：{}", e.getMessage());
        return ApiResult.fail(e.getErrorCode(), e.getMessage());
    }


    /**
     * 最后一道防火墙，不能把后台异常给前端用户看，比如SQL之类
     * 处理所有类型的异常。
     * <p>
     * 该方法通过@ExceptionHandler注解指明处理所有Throwable类型的异常，旨在捕获系统运行时的任何未预期异常。
     * 当系统发生异常时，记录异常信息并返回一个表示系统错误的ApiResult对象。
     *
     * @param e 异常对象，捕获到的任何异常都将作为此参数传递。
     * @return 返回一个ApiResult对象，其中包含系统错误信息。
     */
    @ExceptionHandler(value = Throwable.class)
    public ApiResult<?> throwable(Throwable e) {
        // 记录异常信息，以便于问题追踪和诊断。
        log.error("system exception! The reason is :{}", e.getMessage(), e);
        // 返回系统错误码的ApiResult对象，表示操作失败。
        return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR);
    }
}
