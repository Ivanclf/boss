package com.boss.bossgateway.exception;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(-10)
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(clientException.class)
    public Result<String> errorExceptionHandler(clientException e) {
        log.error("发生异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> commonExceptionHandler(Exception e) {
        log.error("发生系统异常：{}", e.getMessage());
        return Result.error("发生系统异常");
    }

}