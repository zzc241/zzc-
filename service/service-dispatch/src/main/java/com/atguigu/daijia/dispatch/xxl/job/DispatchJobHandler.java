package com.atguigu.daijia.dispatch.xxl.job;

import org.springframework.stereotype.Component;

import com.xxl.job.core.handler.annotation.XxlJob;

@Component
public class DispatchJobHandler {
    
    @XxlJob("firstJobHandler")
    public void testJobHandler() {
        System.out.println("项目集成xxl-job成功！");
    }
}
