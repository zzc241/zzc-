package com.atguigu.daijia.customer.service.impl;


import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.customer.client.CustomerInfoFeignClient;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerInfoFeignClient client;
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Override
    public String login(String code) {
        Result<Long> longResult = client.login(code);

        if(longResult.getCode() != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        Long customerId = longResult.getData();

        if(customerId == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        String token = UUID.randomUUID().toString().replace("-", "");

        //6.把用户id放到redis，设置过期时间
        // redisTemplate.opsForValue().set(token, customerId, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX+token, 
            customerId.toString(), RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        return token;
    }


    @Override
    public CustomerLoginVo getCustomerLoginInfo(String token) { 
        CustomerLoginVo customerLoginVo = new CustomerLoginVo();
        String customerId =(String) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX+token);

        if(!StringUtils.hasText(customerId)){
            throw new RuntimeException("用户登录信息已过期");
        }
        Result<CustomerLoginVo> customerLoginVoResult = client.getCustomerLoginInfo(Long.parseLong(customerId));
        
        Integer code = customerLoginVoResult.getCode();
        if(code != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        customerLoginVo = customerLoginVoResult.getData();
        if(customerLoginVo == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        return customerLoginVo;
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) { 
        CustomerLoginVo customerLoginVo = new CustomerLoginVo();

        Result<CustomerLoginVo> customerLoginVoResult = client.getCustomerLoginInfo(customerId);
        
        Integer code = customerLoginVoResult.getCode();
        if(code != 200){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        customerLoginVo = customerLoginVoResult.getData();
        if(customerLoginVo == null){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        return customerLoginVo;
    }




}
