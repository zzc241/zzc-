package com.atguigu.daijia.order.handle;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.atguigu.daijia.order.service.OrderInfoService;

@Component
public class RedisDelayHandle {
    
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private RedissonClient redissonClient;

    public void listener(){
        new Thread( () -> { 
            while(true){
                RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue("queue_cancel");
                try {
                    // 获取消息
                    String orderId = blockingQueue.take();
                    // 获取订单状态
                    if(StringUtils.hasText (orderId)){
                        orderInfoService.orderCancel(Long.parseLong(orderId));
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
