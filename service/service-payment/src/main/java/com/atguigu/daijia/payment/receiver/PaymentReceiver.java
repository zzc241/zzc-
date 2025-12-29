package com.atguigu.daijia.payment.receiver;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.nacos.shaded.com.google.protobuf.Message;
import com.alibaba.nacos.shaded.io.grpc.Channel;
import com.atguigu.daijia.common.constant.MqConst;
import com.atguigu.daijia.payment.service.WxPayService;

@Component
public class PaymentReceiver {

    @Autowired
    private WxPayService wxPayService;

    @RabbitListener(bindings = @QueueBinding  (
            value = @Queue (value =  MqConst.QUEUE_PAY_SUCCESS , durable = "true" ),
            exchange = @Exchange(value = MqConst.EXCHANGE_ORDER),
            key = {MqConst.ROUTING_PAY_SUCCESS}
    ))
    public void paySuccess(String orderNo, Message message, Channel channel) {
        wxPayService.handleOrder(orderNo);
    }
}