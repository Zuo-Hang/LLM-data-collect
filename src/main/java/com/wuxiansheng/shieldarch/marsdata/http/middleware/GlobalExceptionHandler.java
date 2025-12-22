package com.wuxiansheng.shieldarch.marsdata.http.middleware;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 全局异常处理器
 * 配合RecoveryInterceptor使用，处理Controller层抛出的异常
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // 获取堆栈信息
        String stackTrace = getStackTrace(e);
        
        // 记录错误日志
        log.error("panic_error, errmsg:{}, stack:{}", e.getMessage(), stackTrace);
        
        // 返回错误响应
        ErrorResponse errorResponse = new ErrorResponse(-1, "Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * 错误响应对象
     */
    public static class ErrorResponse {
        private int code;
        private String msg;
        
        public ErrorResponse(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        
        public int getCode() {
            return code;
        }
        
        public void setCode(int code) {
            this.code = code;
        }
        
        public String getMsg() {
            return msg;
        }
        
        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}

