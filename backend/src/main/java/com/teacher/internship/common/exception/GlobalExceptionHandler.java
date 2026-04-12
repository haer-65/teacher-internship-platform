package com.teacher.internship.common.exception;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ApiResponse<Void> handleValidationException(Exception ex) {
        return ApiResponse.fail(ApiCode.BAD_REQUEST.getCode(), "请求参数校验失败");
    }

    @ExceptionHandler({CannotGetJdbcConnectionException.class, MyBatisSystemException.class})
    public ApiResponse<Void> handleDbException(Exception ex) {
        log.error("Database exception", ex);
        return ApiResponse.fail(ApiCode.INTERNAL_ERROR.getCode(), "数据库连接失败，请检查数据库服务与配置（DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD）");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiResponse.fail(ApiCode.INTERNAL_ERROR.getCode(), ApiCode.INTERNAL_ERROR.getMessage());
    }
}
