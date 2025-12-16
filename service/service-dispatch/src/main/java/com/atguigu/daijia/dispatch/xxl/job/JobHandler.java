package com.atguigu.daijia.dispatch.xxl.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atguigu.daijia.dispatch.mapper.XxlJobLogMapper;
import com.atguigu.daijia.dispatch.service.NewOrderService;
import com.atguigu.daijia.model.entity.dispatch.XxlJobLog;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

@Component
public class JobHandler {
    @Autowired
    private XxlJobLogMapper xxlJobLogMapper;
    @Autowired
    private NewOrderService newOrderService;
    @XxlJob("newOrderTaskHandler")
    public void newOrderTaskHandler() {
        XxlJobLog xxlJobLog = new XxlJobLog();
        xxlJobLog.setJobId(XxlJobHelper.getJobId());
        Long startTime = System.currentTimeMillis();
        try {
            newOrderService.executeTask(XxlJobHelper.getJobId());
            xxlJobLog.setStatus(1);
        }catch (Exception e) {
            e.printStackTrace();
            xxlJobLog.setStatus(0);
            xxlJobLog.setError(e.getMessage());
        }
        finally {
            Long endTime = System.currentTimeMillis();
            xxlJobLog.setTimes((int)(endTime - startTime));
            xxlJobLogMapper.insert(xxlJobLog);

        }
    }
}
