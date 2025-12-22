package com.wuxiansheng.shieldarch.marsdata.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxiansheng.shieldarch.marsdata.service.BackstraceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private BackstraceService backstraceService;
    
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    public ApiController() {
        if (this.objectMapper == null) {
            this.objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }

    /**
     * 回溯接口
     */
    @PostMapping("/backstrace")
    public String backstrace(@RequestBody String request) {
        log.info("收到回溯请求: {}", request);
        
        try {
            BackstraceService.BackstraceReq req = objectMapper.readValue(
                request, BackstraceService.BackstraceReq.class);
            backstraceService.backstrace(req);
            return "ok";
        } catch (Exception e) {
            log.error("回溯失败: {}", e.getMessage(), e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
