package com.example.demo.framework.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.example.demo.framework.constant.HttpStatus;
import com.example.demo.framework.exception.ServiceException;
import com.example.demo.framework.exception.SystemException;
import com.example.demo.framework.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

/**
 * @author JiakunXu
 */
@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(value = AccessDeniedException.class)
    public ExceptionResponse exceptionHandler(AccessDeniedException e) {
        log.error("ACCESS_DENIED_EXCEPTION", e);
        return new ExceptionResponse(HttpStatus.FORBIDDEN, "没有权限，请联系管理员授权");
    }

    @ExceptionHandler(value = AsyncRequestTimeoutException.class)
    public ExceptionResponse exceptionHandler(AsyncRequestTimeoutException e) {
        log.error("ASYNC_REQUEST_TIMEOUT_EXCEPTION", e);
        return new ExceptionResponse(HttpStatus.REQUEST_TIMEOUT, e.getMessage());
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ExceptionResponse exceptionHandler(AuthenticationException e) {
        log.error("AUTHENTICATION_EXCEPTION", e);
        return new ExceptionResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(value = BlockException.class)
    public ExceptionResponse exceptionHandler(BlockException e) {
        log.error("BLOCK_EXCEPTION", e);
        return new ExceptionResponse(HttpStatus.TOO_MANY_REQUESTS,
            "Blocked by Sentinel (flow limiting)");
    }

    @ExceptionHandler(value = DataAccessException.class)
    public ExceptionResponse exceptionHandler(DataAccessException e) {
        log.error("DATA_ACCESS_EXCEPTION", e);
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系统正忙，请稍后再试");
    }

    @ExceptionHandler(value = Exception.class)
    public ExceptionResponse exceptionHandler(Exception e) {
        log.error("EXCEPTION", e);
        return new ExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE,
            e == null ? "SERVICE_UNAVAILABLE"
                : (e.getMessage() != null || e.getCause() == null ? e.getMessage()
                    : e.getCause().getMessage()));
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ExceptionResponse exceptionHandler(RuntimeException e) {
        log.error("RUNTIME_EXCEPTION", e);
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            e == null ? "INTERNAL_SERVER_ERROR"
                : (e.getMessage() != null || e.getCause() == null ? e.getMessage()
                    : e.getCause().getMessage()));
    }

    @ExceptionHandler(value = ServiceException.class)
    public ExceptionResponse exceptionHandler(ServiceException e) {
        return new ExceptionResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = SystemException.class)
    public ExceptionResponse exceptionHandler(SystemException e) {
        return new ExceptionResponse(e.getCode(), e.getMessage());
    }

}
