package com.wuxiansheng.shieldarch.marsdata.http.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常恢复处理中间件
 */
@Slf4j
@Component
public class RecoveryInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            return true;
        } catch (Throwable e) {
            handleException(e, request, response);
            return false;
        }
    }
    
    /**
     * 处理异常
     */
    private void handleException(Throwable err, HttpServletRequest request, HttpServletResponse response) {
        if (err == null) {
            return;
        }
        
        // 获取堆栈信息
        String stackTrace = getStackTrace(err);
        
        // 记录错误日志
        log.error("panic_error, errmsg:{}, stack:{}", err.getMessage(), stackTrace);
        
        // 设置响应状态码
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":-1,\"msg\":\"Internal Server Error\"}");
            } catch (Exception e) {
                log.error("Failed to write error response", e);
            }
        }
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
}

