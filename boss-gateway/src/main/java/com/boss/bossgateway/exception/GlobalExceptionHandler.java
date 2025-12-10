package com.boss.bossgateway.exception;

import com.boss.bosscommon.exception.errorException;
import com.boss.bosscommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(-2)
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(errorException.class)
    public Result<String> errorExceptionHandler(errorException e) {
        log.error("发生异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> commonExceptionHandler(Exception e) {
        log.error("发生系统异常：{}", e.getMessage());
        return Result.error("发生系统异常");
    }

}