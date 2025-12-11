package com.atguigu.daijia.driver.service;


import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.entity.driver.DriverInfo;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import com.baomidou.mybatisplus.extension.service.IService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;


public interface DriverInfoService extends IService<DriverInfo> {

    Long login(String code);

    DriverLoginVo getDriverLoginInfo(Long driverId);

    public DriverAuthInfoVo getDriverAuthInfo(Long driverId);

    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm);

    public Boolean createDriverFaceModel(DriverFaceModelForm driverFaceModelForm);

}
