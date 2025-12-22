package com.wuxiansheng.shieldarch.marsdata.llm;

/**
 * 业务工厂接口
 */
public interface BusinessFactory {
    /**
     * 创建业务对象
     * 
     * @param msg 消息内容
     * @return 业务对象，如果消息不需要处理则返回null
     */
    Business createBusiness(String msg);
}

