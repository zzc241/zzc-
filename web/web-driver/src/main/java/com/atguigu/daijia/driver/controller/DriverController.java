package com.atguigu.daijia.driver.controller;

import com.alibaba.nacos.plugin.auth.constant.Constants.Auth;
import com.atguigu.daijia.common.login.zzcLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "司机API接口管理")
@RestController
@RequestMapping(value="/driver")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverController {
    @Autowired
    private DriverService driverInfoService;
    @Autowired
    private DriverService driverService;
	@Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> login(@PathVariable String code) {
        return Result.ok(driverInfoService.login(code));
    }

    @Operation(summary = "获取司机登录信息")
    @zzcLogin
    @GetMapping("/getDriverLoginInfo")
    public Result<DriverLoginVo> getDriverLoginInfo() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverInfoService.getDriverLoginInfo(driverId));
    }


    @Operation(summary = "获取司机认证信息")
    @zzcLogin
    @GetMapping("/getDriverAuthInfo")
    public Result<DriverAuthInfoVo> getDriverAuthInfo() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverInfoService.getDriverAuthInfo(driverId));
    }


    // UpdateDriverAuthInfoForm updateDriverAuthInfoForm
    @Operation(summary = "更新司机认证信息")
    @zzcLogin
    @PostMapping("/updateDriverAuthInfo")
    public Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        updateDriverAuthInfoForm.setDriverId(AuthContextHolder.getUserId());
        // log.info("DriverController收到修改司机认证信息请求，driverId={}", updateDriverAuthInfoForm.getDriverId());
        // log.info("DriverController收到修改司机认证信息请求，updateDriverAuthInfoForm={}", updateDriverAuthInfoForm);
        return Result.ok(driverInfoService.updateDriverAuthInfo(updateDriverAuthInfoForm));
    }
    @Operation(summary = "上传司机人脸模型")
    @zzcLogin
    @PostMapping("/creatDriverFaceModel")
    public Result<Boolean> createDriverFaceModel(@RequestBody DriverFaceModelForm driverFaceModelForm){
        driverFaceModelForm.setDriverId(AuthContextHolder.getUserId());
        log.info("DriverController收到上传司机人脸模型请求，driverFaceModelForm={}", driverFaceModelForm);
        Boolean isSuccess = driverInfoService.createDriverFaceModel(driverFaceModelForm);
        return Result.ok(isSuccess);
    }

    @Operation(summary = "判断司机当日是否进行过人脸识别")
    @zzcLogin
    @GetMapping("/isFaceRecognition")
    Result<Boolean> isFaceRecognition() {
    Long driverId = AuthContextHolder.getUserId();
    return Result.ok(driverService.isFaceRecognition(driverId));
    }

    @Operation(summary = "验证司机人脸")
    @zzcLogin
    @PostMapping("/verifyDriverFace")
    public Result<Boolean> verifyDriverFace(@RequestBody DriverFaceModelForm driverFaceModelForm) {
        driverFaceModelForm.setDriverId(AuthContextHolder.getUserId());
        return Result.ok(driverService.verifyDriverFace(driverFaceModelForm));
    }

    @Operation(summary = "开始接单服务")
    @zzcLogin
    @GetMapping("/startService")
    public Result<Boolean> startService() {
    Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverService.startService(driverId));
    }

    @Operation(summary = "停止接单服务")
    @zzcLogin
    @GetMapping("/stopService")
    public Result<Boolean> stopService() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverService.stopService(driverId));
    }

}

