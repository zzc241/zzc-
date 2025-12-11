package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import com.baomidou.mybatisplus.core.injector.methods.Update;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverInfoFeignClient client;
    @Autowired
    private RedisTemplate redisTemplate;
    @SneakyThrows
    @Override
    public String login(String code) {
        Long driverId = client.login(code).getData();

        String token = UUID.randomUUID().toString().replace("-", "");

        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX +token, driverId.toString(), RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        Result<DriverLoginVo> driverLoginVo = client.getDriverLoginInfo(driverId);
        DriverLoginVo driverLoginVo2 = driverLoginVo.getData();
        return driverLoginVo2;
    }


    @Override
    public DriverAuthInfoVo getDriverAuthInfo(@PathVariable Long driverId) {

        DriverAuthInfoVo driverAuthInfoVo = client.getDriverAuthInfo(driverId).getData();
        return driverAuthInfoVo;
    }

    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        log.info("在updateDriverAuthInfo修改司机认证信息，updateDriverAuthInfoForm={}", updateDriverAuthInfoForm);
        updateDriverAuthInfoForm.setDriverId(AuthContextHolder.getUserId());
        Result<Boolean> updateDriverAuthInfoFormResult = client.updateDriverAuthInfo(updateDriverAuthInfoForm);
        return updateDriverAuthInfoFormResult.getData();
    }

    @Override
    public Boolean createDriverFaceModel(DriverFaceModelForm driverFaceModelForm){
        Result<Boolean> booleanResult = client.createDriverFaceModel(driverFaceModelForm);
        Boolean data = booleanResult.getData();
        return data;
    }

}
