package com.atguigu.daijia.dispatch.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.vo.dispatch.NewOrderTaskVo;
import com.atguigu.daijia.model.vo.order.NewOrderDataVo;

public interface NewOrderService {

    public Long addAndStartTask(NewOrderTaskVo newOrderTaskVo);

    public void executeTask(Long jobId);

    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId);

    public Boolean clearNewOrderQueueData(Long driverId);
}
