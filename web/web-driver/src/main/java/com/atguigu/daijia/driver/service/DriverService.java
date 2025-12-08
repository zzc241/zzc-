package com.atguigu.daijia.driver.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;

import io.swagger.v3.oas.annotations.Operation;

public interface DriverService {

    String login(String code);

    DriverLoginVo getDriverLoginInfo(Long driverId);
    


}
