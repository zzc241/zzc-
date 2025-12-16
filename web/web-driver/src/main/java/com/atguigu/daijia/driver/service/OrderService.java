package com.atguigu.daijia.driver.service;

import java.util.List;

import com.atguigu.daijia.model.vo.order.NewOrderDataVo;

public interface OrderService {

    Integer getOrderStatus(Long orderId);

    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);


}
