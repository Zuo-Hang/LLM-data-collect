package com.wuxiansheng.shieldarch.marsdata.http.config;

import com.wuxiansheng.shieldarch.marsdata.http.middleware.LoggingInterceptor;
import com.wuxiansheng.shieldarch.marsdata.http.middleware.RecoveryInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private LoggingInterceptor loggingInterceptor;
    
    @Autowired
    private RecoveryInterceptor recoveryInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册全局拦截器
        // 注意：拦截器的执行顺序与注册顺序相反
        // 先注册的拦截器后执行，所以RecoveryInterceptor应该先注册（最后执行）
        registry.addInterceptor(recoveryInterceptor)
            .addPathPatterns("/**");
        
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/**");
        
        log.info("HTTP中间件已注册: RecoveryInterceptor, LoggingInterceptor");
    }
}
