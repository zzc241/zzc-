package com.atguigu.daijia.order.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.model.entity.order.OrderInfo;
import com.atguigu.daijia.model.entity.order.OrderStatusLog;
import com.atguigu.daijia.model.enums.OrderStatus;
import com.atguigu.daijia.model.form.order.OrderInfoForm;
import com.atguigu.daijia.model.form.order.StartDriveForm;
import com.atguigu.daijia.model.form.order.UpdateOrderCartForm;
import com.atguigu.daijia.model.vo.order.CurrentOrderInfoVo;
import com.atguigu.daijia.order.mapper.OrderInfoMapper;
import com.atguigu.daijia.order.mapper.OrderStatusLogMapper;
import com.atguigu.daijia.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderStatusLogMapper orderStatusLogMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    
    @Override
    public Long saveOrderInfo(OrderInfoForm orderInfoForm){

        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoForm, orderInfo);

        String orderNo = UUID.randomUUID().toString().replaceAll("-", "");

        orderInfo.setOrderNo(orderNo);

        orderInfo.setStatus(OrderStatus.WAITING_ACCEPT.getStatus());

        orderInfoMapper.insert(orderInfo);

        this.log(orderInfo.getId(),orderInfo.getStatus());
        log.info("保存订单信息，订单号：{}",orderNo);

        redisTemplate.opsForValue().set(RedisConstant.ORDER_ACCEPT_MARK, 
            "0", RedisConstant.ORDER_ACCEPT_MARK_EXPIRES_TIME, TimeUnit.MINUTES);

        return orderInfo.getId();
    }

    public void log(Long orderId, Integer status) {
        OrderStatusLog orderStatusLog = new OrderStatusLog();
        orderStatusLog.setOrderId(orderId);
        orderStatusLog.setOrderStatus(status);
        orderStatusLog.setOperateTime(new Date());
        orderStatusLogMapper.insert(orderStatusLog);
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        LambdaQueryWrapper <OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, orderId);
        queryWrapper.select(OrderInfo::getStatus);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        if(null == orderInfo) {
        //返回null，feign解析会抛出异常，给默认值，后续会用
            return OrderStatus.NULL_ORDER.getStatus();
        }
        return orderInfo.getStatus();
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean robNewOrder(Long driverId, Long orderId) {
        log.info("司机抢单{},{}" , driverId, orderId);
        //抢单成功或取消订单，都会删除该key，redis判断，减少数据库压力
        if(!redisTemplate.hasKey(RedisConstant.ORDER_ACCEPT_MARK)) {
            //抢单失败
            throw new GuiguException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
        }
        RLock lock = redissonClient.getLock(RedisConstant.ROB_NEW_ORDER_LOCK + orderId);
        lock.lock();
        try {
            boolean flag = lock.tryLock(RedisConstant.ROB_NEW_ORDER_LOCK_WAIT_TIME,RedisConstant.ROB_NEW_ORDER_LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if(flag){
                if(!redisTemplate.hasKey(RedisConstant.ORDER_ACCEPT_MARK)) {
                    //抢单失败
                    throw new GuiguException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
                }
                LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(OrderInfo::getId,orderId);
                //乐观锁最重要的：查找status，对应就修改。（其他人也能访问，但是修改不了）
                wrapper.eq(OrderInfo::getStatus, OrderStatus.WAITING_ACCEPT.getStatus());
                OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
                // OrderInfo orderInfo = new OrderInfo();
                orderInfo.setId(orderId);
                orderInfo.setStatus(OrderStatus.ACCEPTED.getStatus());
                orderInfo.setAcceptTime(new Date());
                orderInfo.setDriverId(driverId);
                int rows = orderInfoMapper.updateById(orderInfo);
                if(rows != 1) {
                    //抢单失败
                    throw new GuiguException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
                }
                //记录日志
                this.log(orderId, orderInfo.getStatus());
            }

            //删除redis订单标识
            redisTemplate.delete(RedisConstant.ORDER_ACCEPT_MARK);
        } catch (Exception e) {
            throw new GuiguException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
        } finally {
            if(lock.isLocked()){
                log.info("释放锁");
                lock.unlock();
            }
        }
        //修改订单状态及司机id
        //update order_info set status = 2, driver_id = #{driverId}, accept_time = now() where id = #{id}
        // //修改字段
        // LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(OrderInfo::getId,orderId);
        // OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        // //设置
        // orderInfo.setStatus(OrderStatus.ACCEPTED.getStatus());
        // orderInfo.setDriverId(driverId);
        // orderInfo.setAcceptTime(new Date());


        // LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(OrderInfo::getId,orderId);
        // //乐观锁最重要的：查找status，对应就修改。（其他人也能访问，但是修改不了）
        // wrapper.eq(OrderInfo::getStatus, OrderStatus.WAITING_ACCEPT.getStatus());
        // OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        // // OrderInfo orderInfo = new OrderInfo();
        // orderInfo.setId(orderId);
        // orderInfo.setStatus(OrderStatus.ACCEPTED.getStatus());
        // orderInfo.setAcceptTime(new Date());
        // orderInfo.setDriverId(driverId);
        // int rows = orderInfoMapper.updateById(orderInfo);
        // if(rows != 1) {
        //     //抢单失败
        //     throw new GuiguException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
        // }

        // //记录日志
        // this.log(orderId, orderInfo.getStatus());

        // //删除redis订单标识
        // redisTemplate.delete(RedisConstant.ORDER_ACCEPT_MARK);
        return true;
    }


    @Override
    public CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getCustomerId, customerId);
        Integer[] statusArray = {
            OrderStatus.ACCEPTED.getStatus(),
            OrderStatus.DRIVER_ARRIVED.getStatus(),
            OrderStatus.UPDATE_CART_INFO.getStatus(),
            OrderStatus.START_SERVICE.getStatus(),
            OrderStatus.END_SERVICE.getStatus(),
            OrderStatus.UNPAID.getStatus()
        };
        queryWrapper.in(OrderInfo::getStatus, statusArray);
        queryWrapper.orderByDesc(OrderInfo::getId);
        queryWrapper.last("limit 1");
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        if(null != orderInfo) {
            currentOrderInfoVo.setStatus(orderInfo.getStatus());
            currentOrderInfoVo.setOrderId(orderInfo.getId());
            currentOrderInfoVo.setIsHasCurrentOrder(true);
        } else {
            currentOrderInfoVo.setIsHasCurrentOrder(false);
        }
        return currentOrderInfoVo;
    }
    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getDriverId, driverId);
        //司机发送完账单，司机端主要流程就走完（当前这些节点，司机端会调整到相应的页面处理逻辑）
        Integer[] statusArray = {
            OrderStatus.ACCEPTED.getStatus(),
            OrderStatus.DRIVER_ARRIVED.getStatus(),
            OrderStatus.UPDATE_CART_INFO.getStatus(),
            OrderStatus.START_SERVICE.getStatus(),
            OrderStatus.END_SERVICE.getStatus()
        };
        queryWrapper.in(OrderInfo::getStatus, statusArray);
        queryWrapper.orderByDesc(OrderInfo::getId);
        queryWrapper.last("limit 1");
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        if(null != orderInfo) {
            currentOrderInfoVo.setStatus(orderInfo.getStatus());
            currentOrderInfoVo.setOrderId(orderInfo.getId());
            currentOrderInfoVo.setIsHasCurrentOrder(true);
        } else {
            currentOrderInfoVo.setIsHasCurrentOrder(false);
        }
        return currentOrderInfoVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
    LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(OrderInfo::getId, orderId);
    queryWrapper.eq(OrderInfo::getDriverId, driverId);

    OrderInfo updateOrderInfo = new OrderInfo();
    updateOrderInfo.setStatus(OrderStatus.DRIVER_ARRIVED.getStatus());
    updateOrderInfo.setArriveTime(new Date());
    //只能更新自己的订单
    int row = orderInfoMapper.update(updateOrderInfo, queryWrapper);
    if(row == 1) {
        //记录日志
        this.log(orderId, OrderStatus.DRIVER_ARRIVED.getStatus());
    } else {
        throw new GuiguException(ResultCodeEnum.UPDATE_ERROR);
    }
    return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, updateOrderCartForm.getOrderId());
        queryWrapper.eq(OrderInfo::getDriverId, updateOrderCartForm.getDriverId());

        OrderInfo updateOrderInfo = new OrderInfo();
        BeanUtils.copyProperties(updateOrderCartForm, updateOrderInfo);
        updateOrderInfo.setStatus(OrderStatus.UPDATE_CART_INFO.getStatus());
        //只能更新自己的订单
        int row = orderInfoMapper.update(updateOrderInfo, queryWrapper);
        if(row == 1) {
            //记录日志
            this.log(updateOrderCartForm.getOrderId(), OrderStatus.UPDATE_CART_INFO.getStatus());
        } else {
            throw new GuiguException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean startDrive(StartDriveForm startDriveForm) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, startDriveForm.getOrderId());
        queryWrapper.eq(OrderInfo::getDriverId, startDriveForm.getDriverId());

        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setStatus(OrderStatus.START_SERVICE.getStatus());
        updateOrderInfo.setStartServiceTime(new Date());
        //只能更新自己的订单
        int row = orderInfoMapper.update(updateOrderInfo, queryWrapper);
        if(row == 1) {
            //记录日志
            this.log(startDriveForm.getOrderId(), OrderStatus.START_SERVICE.getStatus());
        } else {
            throw new GuiguException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }

}
